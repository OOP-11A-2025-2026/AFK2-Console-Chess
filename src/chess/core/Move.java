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

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isCapture() {
        return isCapture;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public boolean isPromotion() {
        return isPromotion;
    }

    public Class<?> getPromotionTarget() {
        return promotionTarget;
    }

    @Override
    public String toString() {
        return from.toAlgebraic() + to.toAlgebraic();
    }

    /**
     * Builder class for constructing Move objects.
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

        public Builder(Position from, Position to, Piece movedPiece) {
            this.from = from;
            this.to = to;
            this.movedPiece = movedPiece;
        }

        public Builder capturedPiece(Piece capturedPiece) {
            this.capturedPiece = capturedPiece;
            this.isCapture = capturedPiece != null;
            return this;
        }

        public Builder isCastling(boolean isCastling) {
            this.isCastling = isCastling;
            return this;
        }

        public Builder isEnPassant(boolean isEnPassant) {
            this.isEnPassant = isEnPassant;
            return this;
        }

        public Builder promotion(Class<?> targetPiece) {
            this.isPromotion = targetPiece != null;
            this.promotionTarget = targetPiece;
            return this;
        }

        public Move build() {
            return new Move(this);
        }
    }
}
