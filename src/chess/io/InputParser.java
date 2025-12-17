package chess.io;

import chess.core.*;

/**
 * Parses user input to detect command types and build move objects.
 * Supports both coordinate notation (e2e4) and algebraic notation (e4, Nf3, etc.)
 */
public class InputParser {

    /**
     * Parses user input and determines the command type.
     * 
     * @param input the user input string
     * @return the CommandType, or null if input is empty/invalid
     */
    public static CommandType parseCommandType(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        return CommandType.parseCommand(input.trim());
    }

    /**
     * Parses coordinate notation (e.g., "e2e4") into a Move.
     * Returns null if the notation is invalid.
     * 
     * @param input the coordinate notation string
     * @return a Move.Builder with from and to positions, or null if invalid
     */
    public static Move.Builder parseCoordinateNotation(String input) {
        if (input == null || input.length() != 4) {
            return null;
        }

        String fromSquare = input.substring(0, 2);
        String toSquare = input.substring(2, 4);

        try {
            Position from = Position.fromAlgebraic(fromSquare);
            Position to = Position.fromAlgebraic(toSquare);
            return new Move.Builder(from, to, null); // Piece will be filled in by caller
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parses algebraic notation (e.g., "e4", "Nf3", "Bxc5").
     * This is a simplified parser that extracts the destination square and optional pieces.
     * A full implementation would resolve the move completely.
     * 
     * @param input the algebraic notation string
     * @return a Move.Builder with destination position, or null if invalid
     */
    public static Move.Builder parseAlgebraicNotation(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String notation = input.trim().toLowerCase();

        // Handle castling specially
        if (notation.equals("o-o") || notation.equals("0-0")) {
            return parseKingSideCastling();
        }
        if (notation.equals("o-o-o") || notation.equals("0-0-0")) {
            return parseQueenSideCastling();
        }

        // Remove check/checkmate symbols
        notation = notation.replace("+", "").replace("#", "");

        // Remove capture symbol
        boolean isCapture = notation.contains("x");
        notation = notation.replace("x", "");

        // Extract promotion piece if present
        Class<?> promotionType = null;
        if (notation.length() >= 2 && notation.charAt(notation.length() - 2) == '=') {
            char promotionChar = notation.charAt(notation.length() - 1);
            promotionType = parsePromotionPiece(promotionChar);
            notation = notation.substring(0, notation.length() - 2);
        }

        // Extract destination square (last 2 characters should be the destination)
        if (notation.length() < 2) {
            return null;
        }

        String destSquare = notation.substring(notation.length() - 2);

        try {
            Position to = Position.fromAlgebraic(destSquare);

            // Create a builder with destination
            // Source will be resolved by the game controller
            Move.Builder builder = new Move.Builder(null, to, null);
            
            if (promotionType != null) {
                builder.promotion(promotionType);
            }

            if (isCapture) {
                // Note: Actual captured piece will be determined by board state
            }

            return builder;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parses a promotion piece character.
     * 
     * @param ch the character (Q, R, B, N)
     * @return the piece class, or null if invalid
     */
    private static Class<?> parsePromotionPiece(char ch) {
        ch = Character.toUpperCase(ch);
        switch (ch) {
            case 'Q':
                return chess.pieces.Queen.class;
            case 'R':
                return chess.pieces.Rook.class;
            case 'B':
                return chess.pieces.Bishop.class;
            case 'N':
                return chess.pieces.Knight.class;
            default:
                return null;
        }
    }

    /**
     * Parses king-side castling.
     * 
     * @return a Move.Builder for king-side castling
     */
    private static Move.Builder parseKingSideCastling() {
        // Castling should be resolved by the game controller
        // This just marks it as a castling move attempt
        Move.Builder builder = new Move.Builder(null, null, null);
        builder.isCastling(true);
        return builder;
    }

    /**
     * Parses queen-side castling.
     * 
     * @return a Move.Builder for queen-side castling
     */
    private static Move.Builder parseQueenSideCastling() {
        // Castling should be resolved by the game controller
        // This just marks it as a castling move attempt
        Move.Builder builder = new Move.Builder(null, null, null);
        builder.isCastling(true);
        return builder;
    }

    /**
     * Extracts the piece type from algebraic notation.
     * 
     * @param notation the algebraic notation
     * @return the piece character (K, Q, R, B, N, or null for pawns)
     */
    public static Character extractPieceType(String notation) {
        if (notation == null || notation.isEmpty()) {
            return null;
        }

        char first = Character.toUpperCase(notation.charAt(0));

        if ("KQRBN".indexOf(first) >= 0) {
            return first;
        }

        return null; // Pawn move (no piece letter)
    }

    /**
     * Attempts to parse input as a move.
     * Tries coordinate notation first, then algebraic notation.
     * 
     * @param input the input string
     * @return a Move.Builder, or null if input is not a valid move format
     */
    public static Move.Builder parseMoveInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String trimmed = input.trim().toLowerCase();

        // Try coordinate notation first
        if (trimmed.length() == 4) {
            Move.Builder builder = parseCoordinateNotation(trimmed);
            if (builder != null) {
                return builder;
            }
        }

        // Try algebraic notation
        return parseAlgebraicNotation(trimmed);
    }

    /**
     * Checks if input is a valid move format (coordinate or algebraic).
     * 
     * @param input the input string
     * @return true if it looks like a move, false otherwise
     */
    public static boolean isMove(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // If it's a recognized command, it's not a move
        if (CommandType.isCommand(input)) {
            return false;
        }

        return parseMoveInput(input) != null;
    }

    /**
     * Extracts command-line arguments from input.
     * For commands like "load game.pgn" or "save game.pgn".
     * 
     * @param input the input string
     * @return an array of arguments (split by spaces)
     */
    public static String[] parseArguments(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new String[0];
        }

        return input.trim().split("\\s+");
    }

    /**
     * Extracts the filename from a command like "load game.pgn".
     * 
     * @param input the input string
     * @return the filename, or null if not found
     */
    public static String extractFilename(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String[] args = parseArguments(input);
        return args.length > 1 ? args[1] : null;
    }
}

