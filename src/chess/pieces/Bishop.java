package chess.pieces;

import chess.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bishop piece.
 * Bishops move diagonally any number of squares until blocked.
 */
public class Bishop extends Piece {
    /**
     * Creates a bishop piece.
     * 
     * @param color the color of the bishop (WHITE or BLACK)
     * @param position the initial position of the bishop
     */
    public Bishop(Color color, Position position) {
        super(color, position);
    }

    /**
     * Gets all legal destinations for this bishop from its current position.
     * Bishops move diagonally any number of squares until blocked by a piece.
     * 
     * @param board the current board state
     * @return list of legal destination positions
     */
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

    /**
     * Creates a copy of this bishop at a given position.
     * Preserves the movement history (hasMoved flag) of the original bishop.
     * 
     * @param pos the position for the copied bishop
     * @return a new Bishop instance with the same color and movement history
     */
    @Override
    public Piece copy(Position pos) {
        Bishop copy = new Bishop(color, pos);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    /**
     * Gets the Unicode symbol representing this bishop.
     * 
     * @return white bishop (♗) for white, black bishop (♝) for black
     */
    @Override
    public char getSymbol() {
        return color == Color.WHITE ? '♗' : '♝';
    }
}
