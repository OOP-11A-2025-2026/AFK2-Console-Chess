package chess.rules;

import chess.core.*;

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
        // Special case: allow en passant moves (diagonal pawn moves to empty square with a captured pawn)
        boolean isLegalDestination = piece.getLegalDestinations(board).contains(move.getTo());
        boolean isEnPassant = false;
        
        if (!isLegalDestination && piece instanceof chess.pieces.Pawn && move.isCapture()) {
            // Check if this could be en passant
            if (move.getFrom().getFile() != move.getTo().getFile() && 
                move.getFrom().getRank() != move.getTo().getRank() &&
                board.isEmpty(move.getTo()) &&
                move.getCapturedPiece() instanceof chess.pieces.Pawn) {
                // This looks like en passant
                isEnPassant = true;
                isLegalDestination = true;
            }
        }
        
        if (!isLegalDestination) {
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
        
        // Handle en passant: remove the captured pawn if it's not at the destination
        if (move.isCapture() && move.getCapturedPiece() instanceof chess.pieces.Pawn) {
            // Check if this is en passant (pawn moved diagonally but destination is empty in original board)
            if (move.getFrom().getFile() != move.getTo().getFile() && 
                move.getFrom().getRank() != move.getTo().getRank()) {
                // This is a diagonal pawn move - check if captured piece is on a different square
                Position capturedPos = new Position(move.getTo().getFile(), move.getFrom().getRank());
                if (boardCopy.getPiece(capturedPos) == move.getCapturedPiece()) {
                    // This is en passant - remove the pawn at its actual position
                    boardCopy.removePiece(capturedPos);
                }
            }
        }
        
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
                    if (isSquareAttackedByPiece(board, piecePos, pos, piece)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines whether a particular piece attacks a target square without
     * invoking piece.getLegalDestinations (avoids recursion with King).
     */
    private static boolean isSquareAttackedByPiece(Board board, Position from, Position target, Piece piece) {
        String className = piece.getClass().getSimpleName();

        switch (className) {
            case "Pawn": {
                int dir = piece.getColor() == chess.core.Color.WHITE ? 1 : -1;
                int df = target.getFile() - from.getFile();
                int dr = target.getRank() - from.getRank();
                return dr == dir && Math.abs(df) == 1;
            }
            case "Knight": {
                int df = Math.abs(target.getFile() - from.getFile());
                int dr = Math.abs(target.getRank() - from.getRank());
                return (df == 1 && dr == 2) || (df == 2 && dr == 1);
            }
            case "Bishop": {
                int df = target.getFile() - from.getFile();
                int dr = target.getRank() - from.getRank();
                if (Math.abs(df) != Math.abs(dr) || df == 0) return false;
                return isPathClear(board, from, target);
            }
            case "Rook": {
                int df = target.getFile() - from.getFile();
                int dr = target.getRank() - from.getRank();
                if (df != 0 && dr != 0) return false;
                return isPathClear(board, from, target);
            }
            case "Queen": {
                int df = target.getFile() - from.getFile();
                int dr = target.getRank() - from.getRank();
                if (df != 0 && dr != 0 && Math.abs(df) != Math.abs(dr)) return false;
                return isPathClear(board, from, target);
            }
            case "King": {
                int df = Math.abs(target.getFile() - from.getFile());
                int dr = Math.abs(target.getRank() - from.getRank());
                return df <= 1 && dr <= 1 && (df + dr > 0);
            }
            default:
                return false;
        }
    }}
