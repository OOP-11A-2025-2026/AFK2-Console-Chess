package chess.core;

/**
 * Represents a move in chess, storing the source and destination,
 * the pieces involved, and special move flags.
 */
public class Move {
    private final Position from;
    private final Position to;
    private final Piece movedPiece;
    private final Piece capturedPiece;
    
    private final boolean isCapture;
    private final boolean isCastling;
    private final boolean isEnPassant;
    private final boolean isPromotion;
    private final Class<?> promotionTarget;

    /**
     * Private constructor for Move. Use the Builder pattern via the inner Builder class.
     */
    private Move(Builder builder) {
        if (builder.from == null) {
            throw new IllegalArgumentException("from position must not be null");
        }
        if (builder.to == null) {
            throw new IllegalArgumentException("to position must not be null");
        }
        if (builder.movedPiece == null) {
            throw new IllegalArgumentException("movedPiece must not be null");
        }
        
        this.from = builder.from;
        this.to = builder.to;
        this.movedPiece = builder.movedPiece;
        this.capturedPiece = builder.capturedPiece;
        this.isCapture = builder.isCapture;
        this.isCastling = builder.isCastling;
        this.isEnPassant = builder.isEnPassant;
        this.isPromotion = builder.isPromotion;
        this.promotionTarget = builder.promotionTarget;
    }

    /**
     * Gets the source position of this move.
     * 
     * @return the Position the piece is moving from
     */
    public Position getFrom() {
        return from;
    }

    /**
     * Gets the destination position of this move.
     * 
     * @return the Position the piece is moving to
     */
    public Position getTo() {
        return to;
    }

    /**
     * Gets the piece that is being moved.
     * 
     * @return the Piece that was moved
     */
    public Piece getMovedPiece() {
        return movedPiece;
    }

    /**
     * Gets the piece that was captured by this move, if any.
     * 
     * @return the captured Piece, or null if this move is not a capture
     */
    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    /**
     * Checks if this move is a capture.
     * 
     * @return true if this move captures an opponent's piece
     */
    public boolean isCapture() {
        return isCapture;
    }

    /**
     * Checks if this move is a castling move.
     * 
     * @return true if this is a castling move (king and rook movement)
     */
    public boolean isCastling() {
        return isCastling;
    }

    /**
     * Checks if this move is an en passant capture.
     * 
     * @return true if this is an en passant capture
     */
    public boolean isEnPassant() {
        return isEnPassant;
    }

    /**
     * Checks if this move is a pawn promotion.
     * 
     * @return true if a pawn reaches the promotion rank
     */
    public boolean isPromotion() {
        return isPromotion;
    }

    /**
     * Gets the promotion target piece type if this is a promotion.
     * 
     * @return the Class of the piece to promote to (Queen, Rook, Bishop, Knight),
     *         or null if this is not a promotion
     */
    public Class<?> getPromotionTarget() {
        return promotionTarget;
    }

    @Override
    public String toString() {
        return from.toAlgebraic() + to.toAlgebraic();
    }

    /**
     * Builder class for constructing Move objects using the builder pattern.
     */
    public static class Builder {
        private Position from;
        private Position to;
        private Piece movedPiece;
        private Piece capturedPiece;
        
        private boolean isCapture = false;
        private boolean isCastling = false;
        private boolean isEnPassant = false;
        private boolean isPromotion = false;
        private Class<?> promotionTarget = null;

        /**
         * Creates a new Move builder with source, destination, and piece.
         * 
         * @param from the source position
         * @param to the destination position
         * @param movedPiece the piece being moved
         */
        public Builder(Position from, Position to, Piece movedPiece) {
            this.from = from;
            this.to = to;
            this.movedPiece = movedPiece;
        }

        /**
         * Sets the captured piece (if any).
         * Automatically sets isCapture to true if capturedPiece is not null.
         * 
         * @param capturedPiece the piece being captured, or null for non-captures
         * @return this builder for chaining
         */
        public Builder capturedPiece(Piece capturedPiece) {
            this.capturedPiece = capturedPiece;
            this.isCapture = capturedPiece != null;
            return this;
        }

        /**
         * Marks this as a castling move.
         * 
         * @param isCastling true if this is a castling move
         * @return this builder for chaining
         */
        public Builder isCastling(boolean isCastling) {
            this.isCastling = isCastling;
            return this;
        }

        /**
         * Marks this as an en passant capture.
         * 
         * @param isEnPassant true if this is an en passant capture
         * @return this builder for chaining
         */
        public Builder isEnPassant(boolean isEnPassant) {
            this.isEnPassant = isEnPassant;
            return this;
        }

        /**
         * Sets the pawn promotion target piece type.
         * Automatically sets isPromotion to true if targetPiece is not null.
         * 
         * @param targetPiece the piece class to promote to (Queen, Rook, Bishop, Knight)
         * @return this builder for chaining
         */
        public Builder promotion(Class<?> targetPiece) {
            this.isPromotion = targetPiece != null;
            this.promotionTarget = targetPiece;
            return this;
        }

        /**
         * Builds and returns the Move object.
         * 
         * @return a new Move instance with the configured properties
         */
        public Move build() {
            return new Move(this);
        }
    }
}
