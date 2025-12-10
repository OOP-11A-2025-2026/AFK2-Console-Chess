package chess.pieces;

import chess.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a king piece.
 */
public class King extends Piece {
    public King(Color color, Position position) {
        super(color, position);
    }

    @Override
    public List<Position> getLegalDestinations(Board board) {
        List<Position> destinations = new ArrayList<>();
        
        // King moves one square in any direction
        int[][] directions = {
            {1, 0}, {-1, 0},     // Horizontal
            {0, 1}, {0, -1},     // Vertical
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // Diagonal
        };

        for (int[] dir : directions) {
            int file = position.getFile() + dir[0];
            int rank = position.getRank() + dir[1];
            
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

    @Override
    public Piece copy(Position pos) {
        King copy = new King(color, pos);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    @Override
    public char getSymbol() {
        return color == Color.WHITE ? '♔' : '♚';
    }
}
