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

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

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

    @Override
    public String toString() {
        return getSymbol() + "@" + position.toAlgebraic();
    }
}
