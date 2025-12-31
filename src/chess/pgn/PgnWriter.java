package chess.pgn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Writes chess games to PGN (Portable Game Notation) format files.
 * Handles PGN metadata tags and move records in standard format.
 * This is a final utility class for PGN file export.
 */
public final class PgnWriter
{

    /**
     * Writes a chess game to a PGN file.
     * Includes metadata tags and all moves in standard format.
     * 
     * @param file the output PGN file
     * @param meta the game metadata (Event, Site, Date, etc.)
     * @param moves the list of moves in PGN format
     * @throws IOException if an I/O error occurs while writing
     */
    public void write(File file, PgnGameMetadata meta, List<PgnMoveRecord> moves) throws IOException
    {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(meta, "meta must not be null");
        Objects.requireNonNull(moves, "moves must not be null");

        try (BufferedWriter out = new BufferedWriter(new FileWriter(file)))
        {
            for (Map.Entry<String, String> e : meta.getAllTags().entrySet())
            {
                out.write(String.format("[%s \"%s\"]%n", e.getKey(), escapeTagValue(e.getValue())));
            }
            out.write("\n");

            int count = 0;
            for (PgnMoveRecord rec : moves)
            {
                if (count > 0) out.write(" ");
                out.write(rec.getMoveNumber() + ".");
                if (rec.getWhiteSan() != null) out.write(" " + rec.getWhiteSan());
                if (rec.getBlackSan() != null) out.write(" " + rec.getBlackSan());
                count++;
                if (count % 6 == 0) out.write(System.lineSeparator());
            }

            String result = meta.getTag("Result");
            if (result != null && !result.isEmpty()) {out.write(" " + result);}
            out.write(System.lineSeparator());
            out.flush();
        }
    }

    /**
     * Escapes special characters in PGN tag values.
     * Converts double quotes to escaped form for PGN format compliance.
     * This ensures that tag values containing quotes are properly escaped
     * according to PGN specification.
     * 
     * @param v the tag value to escape
     * @return the escaped tag value, or empty string if input is null
     */
    private String escapeTagValue(String v)
    {
        if (v == null) return "";
        return v.replace("\"", "\\\"");
    }

    /**
     * Creates a PgnWriter instance.
     * Used for writing games to PGN format.
     */
    public PgnWriter() {
        // Utility class for PGN writing
    }
}
