package chess.util;

import chess.core.*;

/**
 * Utility class for algebraic notation conversions.
 */
public class AlgebraicNotationUtil {
    
    private AlgebraicNotationUtil() {
        // Prevent instantiation
    }

    /**
     * Converts a position to algebraic notation (e.g., "e4").
     */
    public static String positionToAlgebraic(Position pos) {
        if (pos == null) {
            return null;
        }
        return pos.toAlgebraic();
    }

    /**
     * Converts algebraic notation to a position.
     */
    public static Position algebraicToPosition(String square) {
        if (square == null || square.isEmpty()) {
            return null;
        }
        try {
            return Position.fromAlgebraic(square);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Converts a move to algebraic notation (e.g., "e2e4").
     */
    public static String moveToCoordinateNotation(Move move) {
        if (move == null) {
            return null;
        }
        return move.getFrom().toAlgebraic() + move.getTo().toAlgebraic();
    }

    /**
     * Converts a file number to a file letter.
     */
    public static char fileToLetter(int file) {
        if (file < 0 || file > 7) {
            return '?';
        }
        return (char) ('a' + file);
    }

    /**
     * Converts a file letter to a file number.
     */
    public static int letterToFile(char file) {
        if (file < 'a' || file > 'h') {
            return -1;
        }
        return file - 'a';
    }

    /**
     * Converts a rank number to a rank digit.
     */
    public static char rankToDigit(int rank) {
        if (rank < 0 || rank > 7) {
            return '?';
        }
        return (char) ('1' + rank);
    }

    /**
     * Converts a rank digit to a rank number.
     */
    public static int digitToRank(char rank) {
        if (rank < '1' || rank > '8') {
            return -1;
        }
        return rank - '1';
    }

    /**
     * Converts castling coordinates to SAN notation.
     * E.g., "e1" "g1" -> "O-O" (white kingside castling)
     * 
     * @param from the from square (e.g., "e1", "e8")
     * @param to the to square (e.g., "g1", "c1", "g8", "c8")
     * @return "O-O" for kingside, "O-O-O" for queenside, or null if not castling
     */
    public static String convertCastlingCoordinateToSan(String from, String to) {
        if (from == null || to == null) {
            return null;
        }
        from = from.trim();
        to = to.trim();

        // White kingside castling
        if (from.equals("e1") && to.equals("g1")) {
            return "O-O";
        }
        // White queenside castling
        if (from.equals("e1") && to.equals("c1")) {
            return "O-O-O";
        }
        // Black kingside castling
        if (from.equals("e8") && to.equals("g8")) {
            return "O-O";
        }
        // Black queenside castling
        if (from.equals("e8") && to.equals("c8")) {
            return "O-O-O";
        }

        return null;
    }

    /**
     * Validates if a string is a valid chess square notation (e.g., "e4", "a1").
     * A valid square must be 2 characters: file (a-h) and rank (1-8).
     * 
     * @param square the square string to validate
     * @return true if the square is valid, false otherwise
     */
    public static boolean isValidSquare(String square) {
        if (square == null || square.length() != 2) {
            return false;
        }

        char file = square.charAt(0);
        char rank = square.charAt(1);

        return file >= 'a' && file <= 'h' && rank >= '1' && rank <= '8';
    }
}
