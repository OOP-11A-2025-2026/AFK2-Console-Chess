package chess.pgn;

/**
 * Custom exception thrown when a PGN file has invalid format.
 * Used for parsing errors and format violations in PGN files.
 */
public class PgnFormatException extends Exception {
    /**
     * Creates a PgnFormatException with a detail message.
     * 
     * @param message the detail message describing the format error
     */
    public PgnFormatException(String message) {
        super(message);
    }

    /**
     * Creates a PgnFormatException with a detail message and cause.
     * 
     * @param message the detail message describing the format error
     * @param cause the underlying cause of the exception
     */
    public PgnFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}

