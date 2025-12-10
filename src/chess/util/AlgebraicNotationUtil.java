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
}
