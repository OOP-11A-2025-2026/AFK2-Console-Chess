package chess.pieces;

import chess.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a knight piece.
 */
public class Knight extends Piece {
    public Knight(Color color, Position position) {
        super(color, position);
    }

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

    @Override
    public Piece copy(Position pos) {
        Knight copy = new Knight(color, pos);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    @Override
    public char getSymbol() {
        return color == Color.WHITE ? '♘' : '♞';
    }
}
