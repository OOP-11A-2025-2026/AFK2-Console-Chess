package chess.pieces;

import chess.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a rook piece.
 * Rooks move horizontally or vertically any number of squares until blocked.
 */
public class Rook extends Piece {
    /**
     * Creates a rook piece.
     * 
     * @param color the color of the rook (WHITE or BLACK)
     * @param position the initial position of the rook
     */
    public Rook(Color color, Position position) {
        super(color, position);
    }

    /**
     * Gets all legal destinations for this rook from its current position.
     * Rooks move horizontally or vertically any number of squares until blocked by a piece.
     * 
     * @param board the current board state
     * @return list of legal destination positions
     */
    @Override
    public List<Position> getLegalDestinations(Board board) {
        List<Position> destinations = new ArrayList<>();
        
        // Horizontal and vertical directions
        int[][] directions = {
            {1, 0}, {-1, 0},   // Horizontal
            {0, 1}, {0, -1}    // Vertical
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

    /**
     * Creates a copy of this rook at a given position.
     * Preserves the movement history (hasMoved flag) of the original rook.
     * 
     * @param pos the position for the copied rook
     * @return a new Rook instance with the same color and movement history
     */
    @Override
    public Piece copy(Position pos) {
        Rook copy = new Rook(color, pos);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    /**
     * Gets the Unicode symbol representing this rook.
     * 
     * @return white rook (♖) for white, black rook (♜) for black
     */
    @Override
    public char getSymbol() {
        return color == Color.WHITE ? '♖' : '♜';
    }
}
