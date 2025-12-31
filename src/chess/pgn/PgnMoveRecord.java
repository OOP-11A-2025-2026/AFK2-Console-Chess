package chess.pgn;

import java.util.Objects;

/**
 * Represents a move record in PGN format (a move pair).
 * Stores the move number and the moves made by white and black in standard algebraic notation.
 */
public final class PgnMoveRecord {
    private final int moveNumber;
    private final String whiteSan;
    private final String blackSan;

    /**
     * Creates a PgnMoveRecord with a move number and optional white and black moves.
     * 
     * @param moveNumber the move number (must be >= 1)
     * @param whiteSan the white player's move in standard algebraic notation, or null
     * @param blackSan the black player's move in standard algebraic notation, or null
     * @throws IllegalArgumentException if moveNumber is less than 1
     */
    public PgnMoveRecord(int moveNumber, String whiteSan, String blackSan) {
        if (moveNumber <= 0) throw new IllegalArgumentException("moveNumber must be >= 1");
        this.moveNumber = moveNumber;
        this.whiteSan = whiteSan != null ? whiteSan.trim() : null;
        this.blackSan = blackSan != null ? blackSan.trim() : null;
    }

    /**
     * Gets the move number.
     * 
     * @return the move number
     */
    public int getMoveNumber() {
        return moveNumber;
    }

    /**
     * Gets the white player's move in standard algebraic notation.
     * 
     * @return the white move, or null if not available
     */
    public String getWhiteSan() {
        return whiteSan;
    }

    /**
     * Gets the black player's move in standard algebraic notation.
     * 
     * @return the black move, or null if not available
     */
    public String getBlackSan() {
        return blackSan;
    }

    /**
     * Returns a string representation of this move record.
     * 
     * @return the move pair formatted as "1. e2e4 c7c5" or similar
     */
    @Override
    public String toString() {
        if (blackSan == null) {return moveNumber + ". " + (whiteSan == null ? "?" : whiteSan);}
        else {return moveNumber + ". " + (whiteSan == null ? "?" : whiteSan) + " " + blackSan;}
    }

    /**
     * Checks equality with another object.
     * Two move records are equal if they have the same move number and moves.
     * 
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PgnMoveRecord)) return false;
        PgnMoveRecord other = (PgnMoveRecord) o;
        return moveNumber == other.moveNumber
                && Objects.equals(whiteSan, other.whiteSan)
                && Objects.equals(blackSan, other.blackSan);
    }

    /**
     * Gets the hash code for this move record.
     * 
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(moveNumber, whiteSan, blackSan);
    }
}

