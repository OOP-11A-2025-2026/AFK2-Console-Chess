package chess.core;

import java.util.List;

/**
 * Abstract base class for all chess pieces.
 * Each piece type implements its own movement pattern.
 */
public abstract class Piece {
    protected Color color;
    protected Position position;
    protected boolean hasMoved;

    /**
     * Constructs a piece with a color and initial position.
     * 
     * @param color the color of the piece
     * @param position the initial position
     */
    protected Piece(Color color, Position position) {
        if (color == null) {
            throw new IllegalArgumentException("Color must not be null");
        }
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null");
        }
        this.color = color;
        this.position = position;
        this.hasMoved = false;
    }

    /**
     * Gets the color of this piece.
     * 
     * @return the color (WHITE or BLACK)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the current position of this piece on the board.
     * 
     * @return the piece's position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Checks if this piece has moved from its starting position.
     * This is used for special rules like castling and pawn double moves.
     * 
     * @return true if the piece has moved, false otherwise
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Sets the position of the piece. Called by the board when moving pieces.
     */
    public void setPosition(Position newPosition) {
        if (newPosition == null) {
            throw new IllegalArgumentException("Position must not be null");
        }
        this.position = newPosition;
        this.hasMoved = true;
    }

    /**
     * Returns all pseudo-legal destinations for this piece from its current position.
     * Pseudo-legal means: respects piece movement rules, but doesn't check if it leaves
     * the king in check (that validation happens in MoveValidator).
     * 
     * @param board the current board state
     * @return list of legal destination positions
     */
    public abstract List<Position> getLegalDestinations(Board board);

    /**
     * Creates a copy of this piece at a given position.
     * Used for board snapshots and deep copies.
     */
    public abstract Piece copy(Position pos);

    /**
     * Returns a single-character symbol for this piece.
     */
    public abstract char getSymbol();

    /**
     * Returns a string representation of this piece.
     * Format: symbol@position (e.g., "♔@e1" for white king at e1)
     * 
     * @return string representation of the piece
     */
    @Override
    public String toString() {
        return getSymbol() + "@" + position.toAlgebraic();
    }
}
