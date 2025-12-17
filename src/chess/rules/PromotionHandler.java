package chess.rules;

import chess.core.*;
import chess.pieces.*;

/**
 * Handles pawn promotion when a pawn reaches the opposite end of the board.
 * Allows the user to choose the promotion piece type (Q, R, B, N).
 */
public class PromotionHandler {

    /**
     * Checks if a move results in a pawn promotion.
     * A pawn promotes when it reaches rank 7 (black) or rank 0 (white).
     * 
     * @param piece the piece being moved
     * @param toPos the destination position
     * @return true if this move should result in promotion, false otherwise
     */
    public static boolean shouldPromote(Piece piece, Position toPos) {
        if (piece == null || toPos == null) {
            return false;
        }

        // Check if it's a pawn
        if (!(piece instanceof Pawn)) {
            return false;
        }

        Color color = piece.getColor();
        int promotionRank = color == Color.WHITE ? 7 : 0;

        return toPos.getRank() == promotionRank;
    }

    /**
     * Promotes a pawn to the specified piece type.
     * 
     * @param board the board
     * @param pawnPos the pawn's position
     * @param promotionType the class of the piece to promote to (Queen, Rook, Bishop, Knight)
     * @throws IllegalArgumentException if promotion type is invalid
     */
    public static void promotePawn(Board board, Position pawnPos, Class<?> promotionType) {
        if (board == null || pawnPos == null || promotionType == null) {
            throw new IllegalArgumentException("Board, position, and promotion type must not be null");
        }

        Piece pawn = board.getPiece(pawnPos);
        if (!(pawn instanceof Pawn)) {
            throw new IllegalArgumentException("No pawn at position " + pawnPos);
        }

        Color color = pawn.getColor();
        Piece promotedPiece = createPromotionPiece(promotionType, color, pawnPos);

        if (promotedPiece == null) {
            throw new IllegalArgumentException("Invalid promotion type: " + promotionType);
        }

        // Replace the pawn with the promoted piece
        board.removePiece(pawnPos);
        board.placePiece(promotedPiece, pawnPos);
    }

    /**
     * Creates a promotion piece of the specified type.
     * 
     * @param type the class type (Queen, Rook, Bishop, Knight)
     * @param color the color of the piece
     * @param position the position of the piece
     * @return the created piece, or null if type is invalid
     */
    public static Piece createPromotionPiece(Class<?> type, Color color, Position position) {
        if (type == null || color == null || position == null) {
            return null;
        }

        if (type == Queen.class) {
            return new Queen(color, position);
        } else if (type == Rook.class) {
            return new Rook(color, position);
        } else if (type == Bishop.class) {
            return new Bishop(color, position);
        } else if (type == Knight.class) {
            return new Knight(color, position);
        }

        return null;
    }

    /**
     * Converts a character promotion choice to a piece class.
     * Valid choices: Q (Queen), R (Rook), B (Bishop), N (Knight)
     * 
     * @param choice the character choice (case-insensitive)
     * @return the piece class, or null if invalid
     */
    public static Class<?> parsePromotionChoice(char choice) {
        choice = Character.toUpperCase(choice);

        switch (choice) {
            case 'Q':
                return Queen.class;
            case 'R':
                return Rook.class;
            case 'B':
                return Bishop.class;
            case 'N':
                return Knight.class;
            default:
                return null;
        }
    }

    /**
     * Converts a piece class to a character representation.
     * 
     * @param type the piece class
     * @return the character (Q, R, B, N), or '?' if invalid
     */
    public static char promotionTypeToChar(Class<?> type) {
        if (type == Queen.class) {
            return 'Q';
        } else if (type == Rook.class) {
            return 'R';
        } else if (type == Bishop.class) {
            return 'B';
        } else if (type == Knight.class) {
            return 'N';
        }
        return '?';
    }

    /**
     * Checks if a promotion type is valid.
     * 
     * @param type the piece class to check
     * @return true if it's a valid promotion type
     */
    public static boolean isValidPromotionType(Class<?> type) {
        return type == Queen.class || type == Rook.class ||
               type == Bishop.class || type == Knight.class;
    }
}
