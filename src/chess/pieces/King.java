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

        // Castling: check king and rook haven't moved, squares are empty, and squares king passes through are not attacked
        if (!this.hasMoved) {
            int kingFile = position.getFile();
            int rank = position.getRank();
            // King-side castling (rook at file 7)
            try {
                Position rookKPos = new Position(7, rank);
                Piece rookK = board.getPiece(rookKPos);
                if (rookK != null && rookK.getClass().getSimpleName().equals("Rook") && rookK.getColor() == color && !rookK.hasMoved()) {
                    Position p1 = new Position(kingFile + 1, rank);
                    Position p2 = new Position(kingFile + 2, rank);
                    if (board.isEmpty(p1) && board.isEmpty(p2)) {
                        if (!chess.rules.MoveValidator.isPositionAttacked(board, position, color.opposite())
                                && !chess.rules.MoveValidator.isPositionAttacked(board, p1, color.opposite())
                                && !chess.rules.MoveValidator.isPositionAttacked(board, p2, color.opposite())) {
                            destinations.add(p2);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // ignore if position creation invalid
            }

            // Queen-side castling (rook at file 0)
            try {
                Position rookQPos = new Position(0, rank);
                Piece rookQ = board.getPiece(rookQPos);
                if (rookQ != null && rookQ.getClass().getSimpleName().equals("Rook") && rookQ.getColor() == color && !rookQ.hasMoved()) {
                    Position q1 = new Position(kingFile - 1, rank);
                    Position q2 = new Position(kingFile - 2, rank);
                    if (board.isEmpty(q1) && board.isEmpty(q2)) {
                        if (!chess.rules.MoveValidator.isPositionAttacked(board, position, color.opposite())
                                && !chess.rules.MoveValidator.isPositionAttacked(board, q1, color.opposite())
                                && !chess.rules.MoveValidator.isPositionAttacked(board, q2, color.opposite())) {
                            destinations.add(q2);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // ignore
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
