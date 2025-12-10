package chess.pieces;

import chess.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bishop piece.
 */
public class Bishop extends Piece {
    public Bishop(Color color, Position position) {
        super(color, position);
    }

    @Override
    public List<Position> getLegalDestinations(Board board) {
        List<Position> destinations = new ArrayList<>();
        
        // Diagonal directions
        int[][] directions = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            addDestinationsInDirection(board, destinations, dir[0], dir[1]);
        }

        return destinations;
    }

    /**
     * Helper method to add destinations in a given direction until blocked.
     */
    private void addDestinationsInDirection(Board board, List<Position> destinations, int fileDir, int rankDir) {
        int file = position.getFile() + fileDir;
        int rank = position.getRank() + rankDir;

        while (file >= 0 && file <= 7 && rank >= 0 && rank <= 7) {
            Position pos = new Position(file, rank);

            if (board.isEmpty(pos)) {
                destinations.add(pos);
            } else if (board.isEnemyPiece(pos, color)) {
                destinations.add(pos);
                break;
            } else {
                break;
            }

            file += fileDir;
            rank += rankDir;
        }
    }

    @Override
    public Piece copy(Position pos) {
        Bishop copy = new Bishop(color, pos);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    @Override
    public char getSymbol() {
        return color == Color.WHITE ? '♗' : '♝';
    }
}
