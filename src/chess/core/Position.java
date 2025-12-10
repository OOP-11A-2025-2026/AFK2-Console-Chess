package chess.core;

/**
 * Represents a square on the chess board.
 * File: 0-7 (a-h)
 * Rank: 0-7 (1-8)
 */
public class Position {
    private final int file;
    private final int rank;

    /**
     * Creates a position at the given file and rank.
     * 
     * @param file the file (0-7, where 0=a, 1=b, ..., 7=h)
     * @param rank the rank (0-7, where 0=1, 1=2, ..., 7=8)
     * @throws IllegalArgumentException if position is invalid
     */
    public Position(int file, int rank) {
        if (!isValidCoordinates(file, rank)) {
            throw new IllegalArgumentException("Invalid position: file=" + file + ", rank=" + rank);
        }
        this.file = file;
        this.rank = rank;
    }

    public int getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    /**
     * Checks if the position is within valid board boundaries.
     */
    public boolean isValid() {
        return isValidCoordinates(file, rank);
    }

    /**
     * Converts position to algebraic notation (e.g., "e4").
     */
    public String toAlgebraic() {
        char fileLetter = (char) ('a' + file);
        int rankNumber = rank + 1;
        return "" + fileLetter + rankNumber;
    }

    /**
     * Creates a Position from algebraic notation (e.g., "e4").
     * 
     * @param square the square in algebraic notation
     * @return the Position object
     * @throws IllegalArgumentException if notation is invalid
     */
    public static Position fromAlgebraic(String square) {
        if (square == null || square.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + square);
        }
        
        char fileLetter = square.charAt(0);
        char rankLetter = square.charAt(1);
        
        if (fileLetter < 'a' || fileLetter > 'h') {
            throw new IllegalArgumentException("Invalid file: " + fileLetter);
        }
        if (rankLetter < '1' || rankLetter > '8') {
            throw new IllegalArgumentException("Invalid rank: " + rankLetter);
        }
        
        int file = fileLetter - 'a';
        int rank = rankLetter - '1';
        
        return new Position(file, rank);
    }

    private static boolean isValidCoordinates(int file, int rank) {
        return file >= 0 && file <= 7 && rank >= 0 && rank <= 7;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) {
            return false;
        }
        Position other = (Position) obj;
        return this.file == other.file && this.rank == other.rank;
    }

    @Override
    public int hashCode() {
        return file * 8 + rank;
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }
}
