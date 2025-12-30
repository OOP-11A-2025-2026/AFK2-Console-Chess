package chess.rules;

import chess.core.*;
import chess.pieces.King;
import chess.pieces.Rook;

/**
 * Handles castling moves (both king-side and queen-side).
 *
 * PERMISSIVE MODE (per your requirement):
 * - DOES NOT check hasMoved() for king/rook.
 * - Only allows castling when king is on e-file start square and rook is on the corner.
 * - Requires squares between are empty.
 * - Requires king is not in check and does not pass through/into check.
 */
public class CastlingHandler {

    /**
     * Determines if a castling move is valid.
     *
     * IMPORTANT: targetRookPos MUST be the rook's CURRENT square (a-file or h-file rook).
     */
    public static boolean isCastlingValid(Board board, Position kingPos, Position targetRookPos) {
        if (board == null || kingPos == null || targetRookPos == null) {
            return false;
        }

        Piece kingPiece = board.getPiece(kingPos);
        Piece rookPiece = board.getPiece(targetRookPos);

        // Must be king + rook
        if (!(kingPiece instanceof King) || !(rookPiece instanceof Rook)) {
            return false;
        }

        // Same color
        if (kingPiece.getColor() != rookPiece.getColor()) {
            return false;
        }

        // Same rank
        if (kingPos.getRank() != targetRookPos.getRank()) {
            return false;
        }

        // Standard starting squares ONLY: King on e-file, rook on a/h-file, correct back rank
        int startRank = kingPiece.getColor() == Color.WHITE ? 0 : 7;
        if (kingPos.getRank() != startRank || kingPos.getFile() != 4) {
            return false;
        }
        if (targetRookPos.getRank() != startRank) {
            return false;
        }
        int rookFile = targetRookPos.getFile();
        if (rookFile != 0 && rookFile != 7) {
            return false;
        }

        // Squares between must be empty
        if (!isPathClear(board, kingPos, targetRookPos)) {
            return false;
        }

        // King cannot be in check
        Color opponent = kingPiece.getColor().opposite();
        if (MoveValidator.isPositionAttacked(board, kingPos, opponent)) {
            return false;
        }

        // King cannot pass through check or end in check
        int kingFile = kingPos.getFile();
        int direction = Integer.compare(rookFile, kingFile);

        Position midPos = new Position(kingFile + direction, kingPos.getRank());
        Position finalPos = new Position(kingFile + 2 * direction, kingPos.getRank());

        if (MoveValidator.isPositionAttacked(board, midPos, opponent)) {
            return false;
        }
        if (MoveValidator.isPositionAttacked(board, finalPos, opponent)) {
            return false;
        }

        return true;
    }

    /**
     * Applies a castling move to the board.
     *
     * IMPORTANT: targetRookPos MUST be the rook's CURRENT square (a/h file corner).
     */
    public static void applyCastling(Board board, Position kingPos, Position targetRookPos) {
        if (board == null || kingPos == null || targetRookPos == null) {
            throw new IllegalArgumentException("Board and positions must not be null");
        }

        Piece king = board.getPiece(kingPos);
        Piece rook = board.getPiece(targetRookPos);

        if (!(king instanceof King) || !(rook instanceof Rook)) {
            throw new IllegalArgumentException("King and rook must exist");
        }

        int kingFile = kingPos.getFile();
        int rookFile = targetRookPos.getFile();
        int rank = kingPos.getRank();

        int direction = Integer.compare(rookFile, kingFile);

        // King goes two squares toward rook
        Position kingNewPos = new Position(kingFile + 2 * direction, rank);

        // Rook goes next to king on the inside
        Position rookNewPos = new Position(kingFile + direction, rank);

        board.movePiece(kingPos, kingNewPos);
        board.movePiece(targetRookPos, rookNewPos);
    }

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
     * Detects a castling attempt.
     * Supports BOTH:
     *  - Standard: king moves two squares (e1g1 / e1c1 / e8g8 / e8c8)
     *  - Legacy: king targets rook square
     */
    public static boolean isCastlingAttempt(Board board, Position kingPos, Position targetPos, Color color) {
        if (board == null || kingPos == null || targetPos == null || color == null) {
            return false;
        }

        Piece piece = board.getPiece(kingPos);
        if (!(piece instanceof King) || piece.getColor() != color) {
            return false;
        }

        int startRank = color == Color.WHITE ? 0 : 7;
        if (kingPos.getRank() != startRank || targetPos.getRank() != startRank) {
            return false;
        }

        int fileDelta = targetPos.getFile() - kingPos.getFile();

        // Standard castling: king moves exactly two squares
        if (Math.abs(fileDelta) == 2) {
            if (kingPos.getFile() != 4) return false;

            int direction = Integer.compare(fileDelta, 0);
            Position rookPos = new Position(direction > 0 ? 7 : 0, startRank);
            Piece rook = board.getPiece(rookPos);
            return (rook instanceof Rook) && rook.getColor() == color;
        }

        // Legacy style: target square is the rook
        Piece targetPiece = board.getPiece(targetPos);
        if (targetPiece instanceof Rook && targetPiece.getColor() == color) {
            return Math.abs(targetPos.getFile() - kingPos.getFile()) > 1;
        }

        return false;
    }

    /**
     * Resolves the rook position from a standard king castling move.
     * e1->g1 => rook h1
     * e1->c1 => rook a1
     */
    public static Position getRookPositionForStandardCastling(Position kingStart, Position kingTarget, Color color) {
        if (kingStart == null || kingTarget == null || color == null) {
            return null;
        }

        int startRank = color == Color.WHITE ? 0 : 7;
        if (kingStart.getRank() != startRank || kingTarget.getRank() != startRank) {
            return null;
        }

        if (kingStart.getFile() != 4) {
            return null;
        }

        int fileDelta = kingTarget.getFile() - kingStart.getFile();
        if (Math.abs(fileDelta) != 2) {
            return null;
        }

        int direction = Integer.compare(fileDelta, 0);
        return new Position(direction > 0 ? 7 : 0, startRank);
    }

    /**
     * Applies standard castling when the UI provides the king destination (two-square king move).
     */
    public static void applyStandardCastling(Board board, Position kingStart, Position kingTarget, Color color) {
        if (board == null || kingStart == null || kingTarget == null || color == null) {
            throw new IllegalArgumentException("Invalid castling parameters");
        }

        Position rookPos = getRookPositionForStandardCastling(kingStart, kingTarget, color);
        if (rookPos == null) {
            throw new IllegalArgumentException("Not a standard castling move");
        }

        if (!isCastlingValid(board, kingStart, rookPos)) {
            throw new IllegalArgumentException("Illegal castling move");
        }

        applyCastling(board, kingStart, rookPos);
    }
}