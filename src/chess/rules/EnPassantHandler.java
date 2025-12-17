package chess.rules;

import chess.core.*;
import chess.pieces.Pawn;

/**
 * Handles en passant captures.
 * Tracks valid en passant squares and applies captures.
 */
public class EnPassantHandler {

    private Position validEnPassantSquare;

    /**
     * Creates an EnPassantHandler.
     */
    public EnPassantHandler() {
        this.validEnPassantSquare = null;
    }

    /**
     * Sets the valid en passant square based on the last move.
     * En passant is only valid if:
     * - A pawn moves two squares forward
     * - The en passant square is the square it passed over
     * 
     * @param lastMove the last move made
     * @return the valid en passant square, or null if no en passant is available
     */
    public Position updateEnPassantSquare(Move lastMove) {
        validEnPassantSquare = null;

        if (lastMove == null) {
            return null;
        }

        Piece movedPiece = lastMove.getMovedPiece();

        // Check if it's a pawn move
        if (!(movedPiece instanceof Pawn)) {
            return null;
        }

        // Check if the pawn moved two squares
        int fromRank = lastMove.getFrom().getRank();
        int toRank = lastMove.getTo().getRank();
        int rankDifference = Math.abs(toRank - fromRank);

        if (rankDifference != 2) {
            return null;
        }

        // The en passant square is the square the pawn passed over
        int enPassantRank = (fromRank + toRank) / 2;
        validEnPassantSquare = new Position(lastMove.getTo().getFile(), enPassantRank);

        return validEnPassantSquare;
    }

    /**
     * Gets the current valid en passant square.
     * 
     * @return the valid en passant square, or null if none
     */
    public Position getValidEnPassantSquare() {
        return validEnPassantSquare;
    }

    /**
     * Sets the valid en passant square manually.
     * 
     * @param square the en passant square, or null
     */
    public void setValidEnPassantSquare(Position square) {
        this.validEnPassantSquare = square;
    }

    /**
     * Checks if an en passant capture is valid.
     * 
     * @param board the current board state
     * @param fromPos the pawn's current position
     * @param toPos the target square (should be the en passant square)
     * @return true if en passant is valid, false otherwise
     */
    public boolean isEnPassantValid(Board board, Position fromPos, Position toPos) {
        if (board == null || fromPos == null || toPos == null) {
            return false;
        }

        // Check if en passant square is set and matches target
        if (validEnPassantSquare == null || !validEnPassantSquare.equals(toPos)) {
            return false;
        }

        Piece piece = board.getPiece(fromPos);

        // Check if it's a pawn
        if (!(piece instanceof Pawn)) {
            return false;
        }

        // Check if target square is empty (characteristic of en passant)
        if (!board.isEmpty(toPos)) {
            return false;
        }

        // Check if there's an enemy pawn to capture next to the moving pawn
        Color color = piece.getColor();
        int captureRank = fromPos.getRank();
        Position capturePos = new Position(toPos.getFile(), captureRank);

        Piece capturedPiece = board.getPiece(capturePos);
        return capturedPiece instanceof Pawn && capturedPiece.getColor() != color;
    }

    /**
     * Applies an en passant capture to the board.
     * Removes the captured pawn from its current square.
     * 
     * @param board the board to apply the capture to
     * @param fromPos the pawn's starting position
     * @param toPos the target square
     */
    public void applyEnPassant(Board board, Position fromPos, Position toPos) {
        if (board == null || fromPos == null || toPos == null) {
            throw new IllegalArgumentException("Board and positions must not be null");
        }

        // Remove the captured pawn (it's on the original rank of the attacking pawn)
        int captureRank = fromPos.getRank();
        Position capturePos = new Position(toPos.getFile(), captureRank);

        Piece capturedPiece = board.getPiece(capturePos);
        if (capturedPiece == null) {
            throw new IllegalArgumentException("No piece to capture at " + capturePos);
        }

        board.removePiece(capturePos);

        // Move the pawn to the en passant square
        Piece pawn = board.getPiece(fromPos);
        if (pawn == null) {
            throw new IllegalArgumentException("No pawn at " + fromPos);
        }

        board.movePiece(fromPos, toPos);
    }

    /**
     * Clears the en passant square (used when a non-pawn move is made).
     */
    public void clearEnPassantSquare() {
        validEnPassantSquare = null;
    }
}
