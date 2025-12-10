package chess.core;

/**
 * Enum representing the two colors in chess.
 */
public enum Color {
    WHITE,
    BLACK;

    /**
     * Returns the opposite color.
     */
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
