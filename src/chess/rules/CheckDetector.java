package chess.rules;

import chess.core.*;
import java.util.List;

/**
 * Detects check and checkmate conditions.
 */
public class CheckDetector {

    /**
     * Checks if a king is in check.
     */
    public boolean isKingInCheck(Board board, Color color) {
        if (board == null || color == null) {
            return false;
        }

        Position kingPos = board.getKingPosition(color);
        if (kingPos == null) {
            return false;
        }

        // Check if the king's position is attacked by any opponent piece
        return MoveValidator.isPositionAttacked(board, kingPos, color.opposite());
    }

    /**
     * Checks if a player has any legal moves.
     */
    public boolean hasAnyLegalMove(Board board, Color color) {
        if (board == null || color == null) {
            return false;
        }

        // Check all pieces of the given color
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                Position piecePos = new Position(file, rank);
                Piece piece = board.getPiece(piecePos);

                if (piece != null && piece.getColor() == color) {
                    List<Position> destinations = piece.getLegalDestinations(board);
                    
                    // Check if any destination results in a legal move
                    for (Position dest : destinations) {
                        Move testMove = new Move.Builder(piecePos, dest, piece).build();
                        if (MoveValidator.isValidMove(board, testMove, color)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if a player is in checkmate.
     */
    public boolean isCheckmate(Board board, Color color) {
        if (board == null || color == null) {
            return false;
        }

        return isKingInCheck(board, color) && !hasAnyLegalMove(board, color);
    }

    /**
     * Checks if a player is in stalemate.
     */
    public boolean isStalemate(Board board, Color color) {
        if (board == null || color == null) {
            return false;
        }

        return !isKingInCheck(board, color) && !hasAnyLegalMove(board, color);
    }
}
