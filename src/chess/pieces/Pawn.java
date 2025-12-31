package chess.pieces;

import chess.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a pawn piece.
 * Pawns are special pieces that move forward and capture diagonally.
 */
public class Pawn extends Piece {
    /**
     * Creates a pawn piece.
     * 
     * @param color the color of the pawn (WHITE or BLACK)
     * @param position the initial position of the pawn
     */
    public Pawn(Color color, Position position) {
        super(color, position);
    }

    /**
     * Gets all legal destinations for this pawn from its current position.
     * Pawns can move forward one square (or two squares from their starting position) 
     * and can capture diagonally one square forward.
     * 
     * @param board the current board state
     * @return list of legal destination positions
     */
    @Override
    public List<Position> getLegalDestinations(Board board) {
        List<Position> destinations = new ArrayList<>();
        int direction = color == Color.WHITE ? 1 : -1;
        int startRank = color == Color.WHITE ? 1 : 6;

        // Forward move (one square)
        int oneForwardRank = position.getRank() + direction;
        if (oneForwardRank >= 0 && oneForwardRank <= 7) {
            Position oneForward = new Position(position.getFile(), oneForwardRank);
            if (board.isEmpty(oneForward)) {
                destinations.add(oneForward);

                // Forward move (two squares from starting position)
                if (position.getRank() == startRank) {
                    int twoForwardRank = position.getRank() + 2 * direction;
                    if (twoForwardRank >= 0 && twoForwardRank <= 7) {
                        Position twoForward = new Position(position.getFile(), twoForwardRank);
                        if (board.isEmpty(twoForward)) {
                            destinations.add(twoForward);
                        }
                    }
                }
            }
        }

        // Captures (diagonal)
        int captureRank = position.getRank() + direction;
        if (captureRank >= 0 && captureRank <= 7) {
            int[] captureFiles = {position.getFile() - 1, position.getFile() + 1};
            for (int file : captureFiles) {
                if (file >= 0 && file <= 7) {
                    Position capPos = new Position(file, captureRank);
                    if (board.isEnemyPiece(capPos, color)) {
                        destinations.add(capPos);
                    }
                }
            }
        }

        return destinations;
    }

    /**
     * Creates a copy of this pawn at a given position.
     * Preserves the movement history (hasMoved flag) of the original pawn.
     * 
     * @param pos the position for the copied pawn
     * @return a new Pawn instance with the same color and movement history
     */
    @Override
    public Piece copy(Position pos) {
        Pawn copy = new Pawn(color, pos);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    /**
     * Gets the Unicode symbol representing this pawn.
     * 
     * @return white pawn (♙) for white, black pawn (♟) for black
     */
    @Override
    public char getSymbol() {
        return color == Color.WHITE ? '♙' : '♟';
    }
}
