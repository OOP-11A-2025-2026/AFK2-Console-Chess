package chess.core;

import chess.pieces.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the 8x8 chess board.
 * The board is the single source of truth for the game state.
 */
public class Board {
    private static final int BOARD_SIZE = 8;
    
    // Map from Position to Piece. Null position means empty square.
    private final Map<Position, Piece> pieces;
    
    private Position whiteKingPosition;
    private Position blackKingPosition;

    /**
     * Creates a new board with all pieces in starting position.
     */
    public Board() {
        this.pieces = new HashMap<>();
        this.whiteKingPosition = null;
        this.blackKingPosition = null;
        initializeStartingPosition();
    }

    /**
     * Private constructor for deep copy.
     */
    private Board(Map<Position, Piece> pieces, Position whiteKingPos, Position blackKingPos) {
        this.pieces = new HashMap<>(pieces.size());
        for (Map.Entry<Position, Piece> entry : pieces.entrySet()) {
            this.pieces.put(entry.getKey(), entry.getValue());
        }
        this.whiteKingPosition = whiteKingPos;
        this.blackKingPosition = blackKingPos;
    }

    /**
     * Initializes the board with pieces in their starting positions.
     */
    private void initializeStartingPosition() {
        // Clear the board
        pieces.clear();

        // White pieces (rank 0 and 1)
        placePiece(new Rook(Color.WHITE, new Position(0, 0)), new Position(0, 0));
        placePiece(new Knight(Color.WHITE, new Position(1, 0)), new Position(1, 0));
        placePiece(new Bishop(Color.WHITE, new Position(2, 0)), new Position(2, 0));
        placePiece(new Queen(Color.WHITE, new Position(3, 0)), new Position(3, 0));
        King whiteKing = new King(Color.WHITE, new Position(4, 0));
        placePiece(whiteKing, new Position(4, 0));
        this.whiteKingPosition = new Position(4, 0);
        placePiece(new Bishop(Color.WHITE, new Position(5, 0)), new Position(5, 0));
        placePiece(new Knight(Color.WHITE, new Position(6, 0)), new Position(6, 0));
        placePiece(new Rook(Color.WHITE, new Position(7, 0)), new Position(7, 0));

        // White pawns
        for (int file = 0; file < BOARD_SIZE; file++) {
            placePiece(new Pawn(Color.WHITE, new Position(file, 1)), new Position(file, 1));
        }

        // Black pieces (rank 7 and 6)
        placePiece(new Rook(Color.BLACK, new Position(0, 7)), new Position(0, 7));
        placePiece(new Knight(Color.BLACK, new Position(1, 7)), new Position(1, 7));
        placePiece(new Bishop(Color.BLACK, new Position(2, 7)), new Position(2, 7));
        placePiece(new Queen(Color.BLACK, new Position(3, 7)), new Position(3, 7));
        King blackKing = new King(Color.BLACK, new Position(4, 7));
        placePiece(blackKing, new Position(4, 7));
        this.blackKingPosition = new Position(4, 7);
        placePiece(new Bishop(Color.BLACK, new Position(5, 7)), new Position(5, 7));
        placePiece(new Knight(Color.BLACK, new Position(6, 7)), new Position(6, 7));
        placePiece(new Rook(Color.BLACK, new Position(7, 7)), new Position(7, 7));

        // Black pawns
        for (int file = 0; file < BOARD_SIZE; file++) {
            placePiece(new Pawn(Color.BLACK, new Position(file, 6)), new Position(file, 6));
        }
    }

    /**
     * Gets the piece at a position, or null if empty.
     * 
     * @param pos the position to check
     * @return the piece at the position, or null if the square is empty
     */
    public Piece getPiece(Position pos) {
        if (pos == null) {
            return null;
        }
        return pieces.get(pos);
    }

    /**
     * Places a piece on the board at the specified position.
     * Updates king position tracking if a king is being placed.
     * 
     * @param piece the piece to place
     * @param position the position to place the piece at
     * @throws IllegalArgumentException if piece or position is null
     */
    public void placePiece(Piece piece, Position position) {
        if (piece == null || position == null) {
            throw new IllegalArgumentException("Piece and position must not be null");
        }
        pieces.put(position, piece);
        
        // Update king positions if needed
        if (piece.getClass().getSimpleName().equals("King")) {
            if (piece.getColor() == Color.WHITE) {
                this.whiteKingPosition = position;
            } else {
                this.blackKingPosition = position;
            }
        }
    }

    /**
     * Removes a piece from the board at the specified position.
     * 
     * @param position the position to remove the piece from
     */
    public void removePiece(Position position) {
        if (position == null) {
            return;
        }
        pieces.remove(position);
    }

    /**
     * Moves a piece from one position to another.
     * Updates king position tracking and returns the captured piece, if any.
     * 
     * @param from the source position
     * @param to the destination position
     * @return the captured piece (if any), or null if this is not a capture
     * @throws IllegalArgumentException if positions are null or no piece at source
     */
    public Piece movePiece(Position from, Position to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Positions must not be null");
        }

        Piece piece = pieces.get(from);
        if (piece == null) {
            throw new IllegalArgumentException("No piece at position " + from);
        }

        Piece captured = pieces.get(to);
        pieces.remove(from);
        piece.setPosition(to);
        pieces.put(to, piece);

        // Update king positions
        if (piece.getClass().getSimpleName().equals("King")) {
            if (piece.getColor() == Color.WHITE) {
                this.whiteKingPosition = to;
            } else {
                this.blackKingPosition = to;
            }
        }

        return captured;
    }

    /**
     * Gets the position of the white king.
     * 
     * @return the position of the white king, or null if not found
     */
    public Position getWhiteKingPosition() {
        return whiteKingPosition;
    }

    /**
     * Gets the position of the black king.
     * 
     * @return the position of the black king, or null if not found
     */
    public Position getBlackKingPosition() {
        return blackKingPosition;
    }

    /**
     * Gets the king position for a given color.
     * 
     * @param color the color (WHITE or BLACK)
     * @return the king position, or null if king not found
     */
    public Position getKingPosition(Color color) {
        return color == Color.WHITE ? whiteKingPosition : blackKingPosition;
    }

    /**
     * Checks if a position is on the board (within valid boundaries).
     * 
     * @param pos the position to check
     * @return true if the position is valid and on the board, false otherwise
     */
    public boolean isOnBoard(Position pos) {
        if (pos == null) {
            return false;
        }
        return pos.isValid();
    }

    /**
     * Checks if a square is empty (contains no piece).
     * 
     * @param pos the position to check
     * @return true if the square is empty, false if it contains a piece
     */
    public boolean isEmpty(Position pos) {
        if (pos == null) {
            return false;
        }
        return pieces.get(pos) == null;
    }

    /**
     * Checks if a square contains an enemy piece (opposite color).
     * 
     * @param pos the position to check
     * @param color the color of the piece making the move
     * @return true if the square contains an enemy piece, false otherwise
     */
    public boolean isEnemyPiece(Position pos, Color color) {
        Piece piece = getPiece(pos);
        return piece != null && piece.getColor() != color;
    }

    /**
     * Checks if a square contains a friendly piece (same color).
     * 
     * @param pos the position to check
     * @param color the color to match
     * @return true if the square contains a friendly piece, false otherwise
     */
    public boolean isFriendlyPiece(Position pos, Color color) {
        Piece piece = getPiece(pos);
        return piece != null && piece.getColor() == color;
    }

    /**
     * Resets the board to the starting position with all pieces initialized.
     */
    public void reset() {
        initializeStartingPosition();
    }

    /**
     * Creates a deep copy of the board with all pieces and state.
     * The copy is independent and modifications to it do not affect the original.
     * 
     * @return a new Board instance that is a deep copy of this board
     */
    public Board copy() {
        Map<Position, Piece> copiedPieces = new HashMap<>();
        for (Map.Entry<Position, Piece> entry : pieces.entrySet()) {
            copiedPieces.put(entry.getKey(), entry.getValue().copy(entry.getKey()));
        }
        return new Board(copiedPieces, whiteKingPosition, blackKingPosition);
    }

    /**
     * Clears the entire board by removing all pieces and resetting king positions.
     */
    public void clear() {
        pieces.clear();
        whiteKingPosition = null;
        blackKingPosition = null;
    }
}
