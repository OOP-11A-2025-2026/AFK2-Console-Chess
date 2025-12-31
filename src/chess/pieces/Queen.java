package chess.pieces;

import chess.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a queen piece.
 * Queens move like both rooks and bishops: horizontally, vertically, or diagonally any number of squares.
 */
public class Queen extends Piece {
    /**
     * Creates a queen piece.
     * 
     * @param color the color of the queen (WHITE or BLACK)
     * @param position the initial position of the queen
     */
    public Queen(Color color, Position position) {
        super(color, position);
    }

    /**
     * Gets all legal destinations for this queen from its current position.
     * Queens move like both rooks and bishops: horizontally, vertically, or diagonally 
     * any number of squares until blocked by a piece.
     * 
     * @param board the current board state
     * @return list of legal destination positions
     */
    @Override
    public List<Position> getLegalDestinations(Board board) {
        List<Position> destinations = new ArrayList<>();
        
        // Queen moves like both rook and bishop: horizontal, vertical, and diagonal
        int[][] directions = {
            {1, 0}, {-1, 0},   // Horizontal
            {0, 1}, {0, -1},   // Vertical
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // Diagonal
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
     * Creates a copy of this queen at a given position.
     * Preserves the movement history (hasMoved flag) of the original queen.
     * 
     * @param pos the position for the copied queen
     * @return a new Queen instance with the same color and movement history
     */
    @Override
    public Piece copy(Position pos) {
        Queen copy = new Queen(color, pos);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    /**
     * Gets the Unicode symbol representing this queen.
     * 
     * @return white queen (♕) for white, black queen (♛) for black
     */
    @Override
    public char getSymbol() {
        return color == Color.WHITE ? '♕' : '♛';
    }
}
