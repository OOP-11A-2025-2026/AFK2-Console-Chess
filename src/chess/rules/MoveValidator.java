package chess.rules;

import chess.core.*;
import chess.pieces.*;

/**
 * Validates chess moves according to FIDE rules.
 */
public class MoveValidator {

    /**
     * Validates a move completely.
     * Checks: piece exists, is correct color, move is pseudo-legal, doesn't leave king in check.
     */
    public static boolean isValidMove(Board board, Move move, Color playerColor) {
        if (board == null || move == null || playerColor == null) {
            return false;
        }

        // Check piece at from position
        Piece piece = board.getPiece(move.getFrom());
        if (piece == null) {
            return false;
        }

        // Check correct color
        if (piece.getColor() != playerColor) {
            return false;
        }

        // Check move is pseudo-legal
        if (!piece.getLegalDestinations(board).contains(move.getTo())) {
            return false;
        }

        // Simulate the move and check if it leaves own king in check
        return !wouldLeaveKingInCheck(board, move, playerColor);
    }

    /**
     * Checks if a move would leave the player's own king in check.
     */
    private static boolean wouldLeaveKingInCheck(Board board, Move move, Color playerColor) {
        Board boardCopy = board.copy();
        
        // Apply the move on the copy
        boardCopy.movePiece(move.getFrom(), move.getTo());
        
        // Check if the player's king is in check
        CheckDetector detector = new CheckDetector();
        return detector.isKingInCheck(boardCopy, playerColor);
    }

    /**
     * Checks if a destination has a piece to capture.
     */
    public static boolean hasCapture(Board board, Position to) {
        if (board == null || to == null) {
            return false;
        }
        return board.getPiece(to) != null;
    }

    /**
     * Checks if the path between two positions is clear (used for sliding pieces).
     */
    public static boolean isPathClear(Board board, Position from, Position to) {
        if (board == null || from == null || to == null) {
            return false;
        }

        int fileDir = Integer.compare(to.getFile(), from.getFile());
        int rankDir = Integer.compare(to.getRank(), from.getRank());

        int file = from.getFile() + fileDir;
        int rank = from.getRank() + rankDir;

        while (file != to.getFile() || rank != to.getRank()) {
            if (file < 0 || file > 7 || rank < 0 || rank > 7) {
                return false;
            }
            Position pos = new Position(file, rank);
            if (!board.isEmpty(pos)) {
                return false;
            }
            file += fileDir;
            rank += rankDir;
        }

        return true;
    }

    /**
     * Checks if a position is attacked by the opponent.
     */
    public static boolean isPositionAttacked(Board board, Position pos, Color opponentColor) {
        if (board == null || pos == null || opponentColor == null) {
            return false;
        }

        // Check all opponent pieces to see if they can attack this position
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                Position piecePos = new Position(file, rank);
                Piece piece = board.getPiece(piecePos);

                if (piece != null && piece.getColor() == opponentColor) {
                    if (piece.getLegalDestinations(board).contains(pos)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
