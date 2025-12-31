package chess.pgn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for PGN (Portable Game Notation) chess files.
 * Reads PGN files and extracts game metadata and moves.
 */
public final class PgnParser {
    private static final Pattern TAG_PATTERN = Pattern.compile("^\\s*\\[(\\w+)\\s+\"(.*)\"\\]\\s*$");
    private static final Pattern MOVE_NUMBER_PATTERN = Pattern.compile("^(\\d+)\\.(?:\\.\\.)?$");
    private static final Pattern RESULT_MARKER_PATTERN = Pattern.compile("^(1-0|0-1|1/2-1/2|\\*)$");
    
    /**
     * Parses a PGN file and extracts game metadata and moves.
     * 
     * @param file the PGN file to parse
     * @return a ParseResult containing metadata and move records
     * @throws IOException if an I/O error occurs
     * @throws PgnFormatException if the PGN format is invalid
     * @throws IllegalArgumentException if file is null
     */
    public ParseResult parse(File file) throws IOException, PgnFormatException
    {
        if (file == null) throw new IllegalArgumentException("file must not be null");
        PgnGameMetadata meta = new PgnGameMetadata();
        List<String> movetextTokens = new ArrayList<>();

        try (BufferedReader r = new BufferedReader(new FileReader(file)))
        {
            String line;
            boolean inTagSection = true;
            StringBuilder movetextBuilder = new StringBuilder();

            while ((line = r.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty())
                {
                    if (!inTagSection) { }
                    inTagSection = false;
                    continue;
                }
                Matcher m = TAG_PATTERN.matcher(line);
                if (m.matches())
                {
                    inTagSection = true;
                    String key = m.group(1);
                    String val = unescapeTagValue(m.group(2));
                    meta.setTag(key, val);
                }
                else
                {
                    inTagSection = false;
                    movetextBuilder.append(line).append(' ');
                }
            }

            String rawMovetext = movetextBuilder.toString().trim();
            if (!rawMovetext.isEmpty())
            {
                rawMovetext = stripCurlyComments(rawMovetext);
                rawMovetext = stripSemicolonComments(rawMovetext);
                rawMovetext = rawMovetext.replaceAll("\\$\\d+", " ");
                rawMovetext = rawMovetext.replaceAll("\\s+", " ").trim();
                if (!rawMovetext.isEmpty())
                {
                    for (String token : rawMovetext.split(" "))
                    {
                        if (token.trim().isEmpty()) continue;
                        if (isResultMarker(token.trim())) continue; // Skip game result markers
                        movetextTokens.add(token.trim());
                    }
                }
            }
        }

        List<PgnMoveRecord> records = tokensToRecords(movetextTokens);
        return new ParseResult(meta, records);
    }

    /**
     * Unescapes special characters in PGN tag values.
     * Converts escaped double quotes to normal form.
     * 
     * @param v the tag value to unescape
     * @return the unescaped tag value
     */
    private String unescapeTagValue(String v) {
        return v.replace("\\\"", "\"");
    }

    /**
     * Removes curly-brace comments from PGN text.
     * Comments in PGN are enclosed in curly braces: {this is a comment}
     * 
     * @param s the string to process
     * @return the string with curly-brace comments removed
     */
    private String stripCurlyComments(String s)
    {
        StringBuilder out = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (c == '{') { depth++; continue; }
            if (c == '}') { if (depth > 0) depth--; continue; }
            if (depth == 0) out.append(c);
        }
        return out.toString();
    }

    /**
     * Removes semicolon comments from PGN text.
     * Semicolons to end of line are comments (currently not implemented).
     * 
     * @param s the string to process
     * @return the string with semicolon comments removed
     */
    private String stripSemicolonComments(String s) {return s;}

    /**
     * Converts a list of tokens into PgnMoveRecord objects.
     * Groups move tokens by move number and separates white/black moves.
     * 
     * @param tokens the list of tokens (move numbers, moves, etc.)
     * @return a list of PgnMoveRecords
     * @throws PgnFormatException if move number parsing fails
     */
    private List<PgnMoveRecord> tokensToRecords(List<String> tokens) throws PgnFormatException
    {
        List<PgnMoveRecord> records = new ArrayList<>();
        int i = 0;
        int expectedMoveNumber = 1;

        while (i < tokens.size())
        {
            String token = tokens.get(i);
            Matcher mn = MOVE_NUMBER_PATTERN.matcher(token);
            if (mn.matches())
            {
                try
                {
                    int n = Integer.parseInt(mn.group(1));
                    expectedMoveNumber = n;
                }
                catch (NumberFormatException ex)
                {
                    throw new PgnFormatException("Invalid move number: " + token);
                }
                i++;
                String white = null, black = null;
                if (i < tokens.size())
                {
                    String maybe = tokens.get(i);
                    if (!isMoveNumberToken(maybe))
                    {
                        white = maybe;
                        i++;
                    }
                }
                if (i < tokens.size())
                {
                    String maybe2 = tokens.get(i);
                    if (!isMoveNumberToken(maybe2))
                    {
                        black = maybe2;
                        i++;
                    }
                }
                records.add(new PgnMoveRecord(expectedMoveNumber, white, black));
                expectedMoveNumber++;
            }
            else
            {
                String white = token;
                i++;
                String black = null;
                if (i < tokens.size())
                {
                    String next = tokens.get(i);
                    if (!isMoveNumberToken(next))
                    {
                        black = next;
                        i++;
                    }
                }
                records.add(new PgnMoveRecord(expectedMoveNumber, white, black));
                expectedMoveNumber++;
            }
        }
        return records;
    }

    /**
     * Checks if a token matches the move number pattern.
     * 
     * @param t the token to check
     * @return true if the token is a move number (e.g., "1.", "2.", "25.")
     */
    private boolean isMoveNumberToken(String t) {return MOVE_NUMBER_PATTERN.matcher(t).matches();}

    /**
     * Checks if a token is a game result marker.
     * Valid result markers are: 1-0 (white wins), 0-1 (black wins), 1/2-1/2 (draw), * (unknown)
     * 
     * @param t the token to check
     * @return true if the token is a result marker
     */
    private boolean isResultMarker(String t) {return RESULT_MARKER_PATTERN.matcher(t).matches();}

    /**
     * Inner class representing the result of parsing a PGN file.
     * Encapsulates the extracted metadata and moves.
     */
    public static final class ParseResult
    {
        private final PgnGameMetadata metadata;
        private final List<PgnMoveRecord> moves;

        /**
         * Creates a ParseResult with metadata and moves.
         * 
         * @param metadata the game metadata extracted from PGN tags
         * @param moves the list of move records extracted from the PGN
         */
        public ParseResult(PgnGameMetadata metadata, List<PgnMoveRecord> moves)
        {
            this.metadata = metadata;
            this.moves = moves;
        }

        /**
         * Gets the game metadata.
         * 
         * @return the PgnGameMetadata containing Event, Site, Date, and other tags
         */
        public PgnGameMetadata getMetadata() {return metadata;}

        /**
         * Gets the list of moves.
         * 
         * @return the list of PgnMoveRecords representing the game moves
         */
        public List<PgnMoveRecord> getMoves() {return moves;}
    }
}
