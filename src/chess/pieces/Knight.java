package chess.pieces;

import chess.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a knight piece.
 * Knights move in an L-shaped pattern: 2 squares in one direction and 1 square perpendicular.
 * Knights are the only pieces that can jump over other pieces.
 */
public class Knight extends Piece {
    /**
     * Creates a knight piece.
     * 
     * @param color the color of the knight (WHITE or BLACK)
     * @param position the initial position of the knight
     */
    public Knight(Color color, Position position) {
        super(color, position);
    }

    /**
     * Gets all legal destinations for this knight from its current position.
     * Knights move in an L-shape: 2 squares in one direction, 1 in the perpendicular direction.
     * Knights can jump over other pieces.
     * 
     * @param board the current board state
     * @return list of legal destination positions
     */
    @Override
    public List<Position> getLegalDestinations(Board board) {
        List<Position> destinations = new ArrayList<>();
        
        // Knight moves in an L-shape: 2 squares in one direction, 1 in the other
        int[][] offsets = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] offset : offsets) {
            int file = position.getFile() + offset[0];
            int rank = position.getRank() + offset[1];
            
            // Check if position is on the board
            if (file >= 0 && file <= 7 && rank >= 0 && rank <= 7) {
                Position pos = new Position(file, rank);
                if (board.isEmpty(pos) || board.isEnemyPiece(pos, color)) {
                    destinations.add(pos);
                }
            }
        }

        return destinations;
    }

    /**
     * Creates a copy of this knight at a given position.
     * Preserves the movement history (hasMoved flag) of the original knight.
     * 
     * @param pos the position for the copied knight
     * @return a new Knight instance with the same color and movement history
     */
    @Override
    public Piece copy(Position pos) {
        Knight copy = new Knight(color, pos);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    /**
     * Gets the Unicode symbol representing this knight.
     * 
     * @return white knight (♘) for white, black knight (♞) for black
     */
    @Override
    public char getSymbol() {
        return color == Color.WHITE ? '♘' : '♞';
    }
}
