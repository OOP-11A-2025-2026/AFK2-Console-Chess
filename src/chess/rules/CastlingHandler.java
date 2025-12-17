package chess.rules;

import chess.core.*;
import chess.pieces.King;
import chess.pieces.Rook;

/**
 * Handles castling moves (both king-side and queen-side).
 * Validates castling conditions and applies the special move logic.
 */
public class CastlingHandler {

    /**
     * Determines if a castling move is valid.
     * Validates all castling conditions:
     * - King and rook haven't moved
     * - No pieces between king and rook
     * - King is not in check
     * - King doesn't move through check
     * - King doesn't land in check
     * 
     * @param board the current board state
     * @param kingPos the king's current position
     * @param targetRookPos the rook position (for king-side or queen-side)
     * @return true if castling is valid, false otherwise
     */
    public static boolean isCastlingValid(Board board, Position kingPos, Position targetRookPos) {
        if (board == null || kingPos == null || targetRookPos == null) {
            return false;
        }

        Piece kingPiece = board.getPiece(kingPos);
        Piece rookPiece = board.getPiece(targetRookPos);

        // Check if king and rook exist and are correct types
        if (!(kingPiece instanceof King) || !(rookPiece instanceof Rook)) {
            return false;
        }

        // Check if they have the same color
        if (kingPiece.getColor() != rookPiece.getColor()) {
            return false;
        }

        // Check if both pieces haven't moved
        if (kingPiece.hasMoved() || rookPiece.hasMoved()) {
            return false;
        }

        // Check if there are no pieces between king and rook
        if (!isPathClear(board, kingPos, targetRookPos)) {
            return false;
        }

        // Check if king is in check
        if (MoveValidator.isPositionAttacked(board, kingPos, kingPiece.getColor().opposite())) {
            return false;
        }

        // Check if king moves through check or into check
        int kingFile = kingPos.getFile();
        int rookFile = targetRookPos.getFile();
        int direction = Integer.compare(rookFile, kingFile);

        // King moves two squares
        int midFile = kingFile + direction;
        int finalFile = kingFile + 2 * direction;

        // Check intermediate square and final square are not attacked
        Position midPos = new Position(midFile, kingPos.getRank());
        Position finalPos = new Position(finalFile, kingPos.getRank());

        Color opponentColor = kingPiece.getColor().opposite();
        
        if (MoveValidator.isPositionAttacked(board, midPos, opponentColor)) {
            return false;
        }

        if (MoveValidator.isPositionAttacked(board, finalPos, opponentColor)) {
            return false;
        }

        return true;
    }

    /**
     * Applies a castling move to the board.
     * Moves both the king and the rook to their new positions.
     * 
     * @param board the board to apply the move to
     * @param kingPos the king's current position
     * @param targetRookPos the rook's position
     */
    public static void applyCastling(Board board, Position kingPos, Position targetRookPos) {
        if (board == null || kingPos == null || targetRookPos == null) {
            throw new IllegalArgumentException("Board and positions must not be null");
        }

        Piece king = board.getPiece(kingPos);
        Piece rook = board.getPiece(targetRookPos);

        if (king == null || rook == null) {
            throw new IllegalArgumentException("King and rook must exist");
        }

        int kingFile = kingPos.getFile();
        int rookFile = targetRookPos.getFile();
        int rank = kingPos.getRank();

        // Determine direction
        int direction = Integer.compare(rookFile, kingFile);

        // Move king two squares
        int kingNewFile = kingFile + 2 * direction;
        Position kingNewPos = new Position(kingNewFile, rank);

        // Move rook next to king
        int rookNewFile = kingFile + direction;
        Position rookNewPos = new Position(rookNewFile, rank);

        // Apply moves
        board.movePiece(kingPos, kingNewPos);
        board.movePiece(targetRookPos, rookNewPos);
    }

    /**
     * Checks if the path between two positions is clear.
     * 
     * @param board the board
     * @param from the starting position
     * @param to the ending position
     * @return true if the path is clear, false otherwise
     */
    private static boolean isPathClear(Board board, Position from, Position to) {
        int fileDir = Integer.compare(to.getFile(), from.getFile());
        int rankDir = Integer.compare(to.getRank(), from.getRank());

        int file = from.getFile() + fileDir;
        int rank = from.getRank() + rankDir;

        while (!(file == to.getFile() && rank == to.getRank())) {
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
     * Determines if a king-to-rook move represents a castling attempt.
     * Checks if the king is on its starting rank and moving to/past a rook.
     * 
     * @param board the board
     * @param kingPos the king's position
     * @param targetPos the target position
     * @param color the player's color
     * @return true if this looks like a castling move, false otherwise
     */
    public static boolean isCastlingAttempt(Board board, Position kingPos, Position targetPos, Color color) {
        if (board == null || kingPos == null || targetPos == null || color == null) {
            return false;
        }

        Piece piece = board.getPiece(kingPos);
        if (!(piece instanceof King)) {
            return false;
        }

        // King must be on starting rank
        int startRank = color == Color.WHITE ? 0 : 7;
        if (kingPos.getRank() != startRank) {
            return false;
        }

        // Target must be on same rank
        if (targetPos.getRank() != startRank) {
            return false;
        }

        // Target must be a rook of the same color
        Piece targetPiece = board.getPiece(targetPos);
        if (!(targetPiece instanceof Rook) || targetPiece.getColor() != color) {
            return false;
        }

        // Must be more than one square away (normal king move is one square)
        return Math.abs(targetPos.getFile() - kingPos.getFile()) > 1;
    }
}
