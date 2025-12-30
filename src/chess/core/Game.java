package chess.core;

import chess.rules.*;
import chess.io.InputParser;
import chess.util.AlgebraicNotationUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chess game, holding the board, players, and game history.
 */
public class Game {
    private final Board board;
    private final Player whitePlayer;
    private final Player blackPlayer;
    private final ChessClock clock;
    
    private List<Move> moveHistory;
    private Color currentPlayer;
    private GameState gameState;
    private boolean drawOfferPending;
    private Color drawOfferer;

    /**
     * Creates a new game with two players and a clock.
     * 
     * @param whitePlayer the white player
     * @param blackPlayer the black player
     * @param clock the chess clock
     */
    public Game(Player whitePlayer, Player blackPlayer, ChessClock clock) {
        if (whitePlayer == null || blackPlayer == null || clock == null) {
            throw new IllegalArgumentException("Players and clock must not be null");
        }
        if (whitePlayer.getColor() != Color.WHITE || blackPlayer.getColor() != Color.BLACK) {
            throw new IllegalArgumentException("Player colors must match their assignments");
        }

        this.board = new Board();
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.clock = clock;
        
        this.moveHistory = new ArrayList<>();
        this.currentPlayer = Color.WHITE;
        this.gameState = GameState.ONGOING;
        this.drawOfferPending = false;
        this.drawOfferer = null;
    }

    /**
     * Creates a new game from a FEN string (for future use, currently uses default board).
     * TODO: Implement full FEN parsing
     */
    public Game(Player whitePlayer, Player blackPlayer, String fenString) {
        this(whitePlayer, blackPlayer, new ChessClock(5 * 60 * 1000));
        // FEN parsing not yet fully implemented; would need FenUtil enhancement
    }

    public Board getBoard() {
        return board;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer == Color.WHITE ? whitePlayer : blackPlayer;
    }

    public Color getCurrentPlayerColor() {
        return currentPlayer;
    }

    public ChessClock getClock() {
        return clock;
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("Game state must not be null");
        }
        this.gameState = state;
    }

    public boolean isDrawOfferPending() {
        return drawOfferPending;
    }

    public Color getDrawOfferer() {
        return drawOfferer;
    }

    /**
     * Offers a draw from the current player.
     */
    public void offerDraw() {
        this.drawOfferPending = true;
        this.drawOfferer = currentPlayer;
    }

    /**
     * Accepts the pending draw offer.
     */
    public void acceptDraw() {
        if (drawOfferPending) {
            setGameState(GameState.DRAW_BY_AGREEMENT);
        }
    }

    /**
     * Rejects the pending draw offer.
     */
    public void rejectDraw() {
        this.drawOfferPending = false;
        this.drawOfferer = null;
    }

    /**
     * Adds a move to the history and switches the current player.
     */
    public void addMove(Move move) {
        if (move == null) {
            throw new IllegalArgumentException("Move must not be null");
        }
        moveHistory.add(move);
        switchPlayer();
    }

    public void clearDrawOffer() {
        this.drawOfferPending = false;
        this.drawOfferer = null;
    }

    /**
     * Switches the current player.
     */
    public void switchPlayer() {
        currentPlayer = currentPlayer.opposite();
    }

    /**
     * Undoes the last move and reverts the current player.
     */
    public void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            moveHistory.remove(moveHistory.size() - 1);
            switchPlayer();
            drawOfferPending = false;
            drawOfferer = null;
        }
    }

    /**
     * Applies a move from coordinate notation (e.g., "e2e4").
     * 
     * @param fromSquare the source square (e.g., "e2")
     * @param toSquare the destination square (e.g., "e4")
     * @throws IllegalArgumentException if move is invalid
     */
    public void applyMove(String fromSquare, String toSquare) {
        Position from = Position.fromAlgebraic(fromSquare);
        Position to = Position.fromAlgebraic(toSquare);
        applyMove(from, to);
    }

    /**
     * Applies a move from positions.
     * 
     * @param from the source position
     * @param to the destination position
     * @throws IllegalArgumentException if move is invalid
     */
    public void applyMove(Position from, Position to) {
        Piece piece = board.getPiece(from);
        if (piece == null) {
            throw new IllegalArgumentException("No piece at " + from);
        }
        if (piece.getColor() != currentPlayer) {
            throw new IllegalArgumentException("Cannot move opponent's piece");
        }

        Piece capturedPiece = board.getPiece(to);
        Move move = new Move.Builder(from, to, piece)
                .capturedPiece(capturedPiece)
                .build();

        if (!MoveValidator.isValidMove(board, move, currentPlayer)) {
            throw new IllegalArgumentException("Illegal move: " + from + to);
        }

        // Apply the move
        board.movePiece(from, to);
        addMove(move);
    }

    /**
     * Applies a move from a Move object.
     * 
     * @param move the move to apply
     * @throws IllegalArgumentException if move is invalid
     */
    public void applyMove(Move move) {
        if (move == null) {
            throw new IllegalArgumentException("Move must not be null");
        }
        if (!MoveValidator.isValidMove(board, move, currentPlayer)) {
            throw new IllegalArgumentException("Illegal move");
        }

        // Handle castling specially - need to move both king and rook
        if (move.isCastling()) {
            Position kingFrom = move.getFrom();
            Position kingTo = move.getTo();
            
            // Determine rook positions
            int rookFromFile = kingFrom.getFile() < kingTo.getFile() ? 7 : 0; // King-side vs Queen-side
            Position rookFrom = new Position(rookFromFile, kingFrom.getRank());
            int rookToFile = kingFrom.getFile() < kingTo.getFile() ? 5 : 3; // Rook lands at f1/f8 or d1/d8
            Position rookTo = new Position(rookToFile, kingFrom.getRank());
            
            // Move both pieces
            board.movePiece(kingFrom, kingTo);
            board.movePiece(rookFrom, rookTo);
        } else {
            // Normal move
            board.movePiece(move.getFrom(), move.getTo());
        }
        
        addMove(move);
    }

    /**
     * Applies a move from standard algebraic notation (SAN).
     * This is a simplified implementation that may not handle all edge cases.
     * 
     * @param san the move in algebraic notation (e.g., "e4", "Nf3", "Bxc5")
     * @throws IllegalArgumentException if move is invalid
     */
    public void applySan(String san) {
        Move move = resolveSan(san);
        if (move == null) {
            throw new IllegalArgumentException("Cannot resolve move: " + san);
        }
        applyMove(move);
    }

    /**
     * Resolves a move from standard algebraic notation.
     * Handles: e4, Nf3, Bxc5, exd6, O-O, O-O-O, etc.
     * 
     * @param san the move in algebraic notation
     * @return the Move object, or null if it cannot be resolved
     */
    public Move resolveSan(String san) {
        if (san == null || san.trim().isEmpty()) {
            return null;
        }

        san = san.trim();

        // Handle castling
        if (san.equals("O-O") || san.equals("0-0")) {
            return resolveCastling(true); // King-side
        }
        if (san.equals("O-O-O") || san.equals("0-0-0")) {
            return resolveCastling(false); // Queen-side
        }

        // Try coordinate notation first (e.g., "e2e4", "a2a4")
        if (san.length() == 4 && Character.isLetter(san.charAt(0)) && Character.isDigit(san.charAt(1))) {
            try {
                Position from = Position.fromAlgebraic(san.substring(0, 2));
                Position to = Position.fromAlgebraic(san.substring(2, 4));
                Piece piece = board.getPiece(from);
                if (piece != null && piece.getColor() == currentPlayer) {
                    Piece captured = board.getPiece(to);
                    Move move = new Move.Builder(from, to, piece).capturedPiece(captured).build();
                    if (MoveValidator.isValidMove(board, move, currentPlayer)) {
                        return move;
                    }
                }
            } catch (IllegalArgumentException e) {
                // Not valid coordinate notation
            }
        }

        // Parse algebraic notation: [Piece][From][x]To[=Promotion][+#]
        // Remove check/checkmate symbols
        san = san.replace("+", "").replace("#", "");

        // Extract promotion if present
        Class<?> promotionType = null;
        int promoteIdx = san.indexOf('=');
        if (promoteIdx >= 0) {
            char promotionChar = san.charAt(promoteIdx + 1);
            promotionType = chess.rules.PromotionHandler.parsePromotionChoice(promotionChar);
            san = san.substring(0, promoteIdx);
        }

        // Check for capture
        boolean isCapture = san.contains("x");
        san = san.replace("x", "");

        // Extract destination square (last 2 chars)
        if (san.length() < 2) {
            return null;
        }

        String destSquare = san.substring(san.length() - 2);
        String hint = san.substring(0, san.length() - 2); // Piece type and disambiguation

        try {
            Position toPos = Position.fromAlgebraic(destSquare);

            // Determine if a piece type is specified
            String targetPieceLetter = "";
            String disambiguation = "";
            
            if (hint.length() > 0 && Character.isUpperCase(hint.charAt(0))) {
                // Piece type is specified (N, B, R, Q, K)
                targetPieceLetter = String.valueOf(hint.charAt(0));
                disambiguation = hint.substring(1);
            } else if (hint.length() > 0) {
                // No piece type specified - this is either a pawn move with file (exd4)
                // or a pawn simple move (e4, d5, etc.) where hint would be file for captures
                disambiguation = hint;
            }

            // Find all pieces that can move to destination
            java.util.List<Move> candidates = new java.util.ArrayList<>();

            for (int f = 0; f < 8; f++) {
                for (int r = 0; r < 8; r++) {
                    Position pos = new Position(f, r);
                    Piece piece = board.getPiece(pos);

                    if (piece == null || piece.getColor() != currentPlayer) {
                        continue;
                    }

                    // Get piece type for this piece
                    String className = piece.getClass().getSimpleName();
                    String pieceLetter;
                    switch (className) {
                        case "Knight":
                            pieceLetter = "N";
                            break;
                        case "Bishop":
                            pieceLetter = "B";
                            break;
                        case "Rook":
                            pieceLetter = "R";
                            break;
                        case "Queen":
                            pieceLetter = "Q";
                            break;
                        case "King":
                            pieceLetter = "K";
                            break;
                        case "Pawn":
                            pieceLetter = "";
                            break;
                        default:
                            pieceLetter = "";
                    }

                    // Filter by piece type if specified
                    if (!targetPieceLetter.isEmpty()) {
                        // Piece type specified, only match that piece type
                        if (!pieceLetter.equals(targetPieceLetter)) {
                            continue;
                        }
                    } else {
                        // No piece type specified
                        // For simple moves without capture ("e4", "d5", "f3"), prefer pawns
                        // For captures with file ("exd4", "axb5"), only pawns
                        // So: only consider pawns if no piece type is specified
                        if (!className.equals("Pawn")) {
                            continue;
                        }
                    }

                    // Check if piece can move to destination
                    if (piece.getLegalDestinations(board).contains(toPos)) {
                        Piece captured = board.getPiece(toPos);
                        Move move = new Move.Builder(pos, toPos, piece)
                                .capturedPiece(captured)
                                .promotion(promotionType)
                                .build();

                        if (MoveValidator.isValidMove(board, move, currentPlayer)) {
                            candidates.add(move);
                        }
                    }
                }
            }

            // Filter by disambiguation if needed
            if (!disambiguation.isEmpty() && candidates.size() > 1) {
                java.util.List<Move> filtered = new java.util.ArrayList<>();
                
                for (Move candidate : candidates) {
                    boolean matches = false;
                    char disambigChar = disambiguation.charAt(0);
                    
                    if (disambigChar >= 'a' && disambigChar <= 'h') {
                        // File disambiguation (e.g., "Nbd2" or "exd4")
                        int fromFile = Character.getNumericValue(disambigChar) - Character.getNumericValue('a');
                        if (candidate.getFrom().getFile() == fromFile) {
                            matches = true;
                        }
                    } else if (disambigChar >= '1' && disambigChar <= '8') {
                        // Rank disambiguation (e.g., "N1d2")
                        int fromRank = Character.getNumericValue(disambigChar) - 1;
                        if (candidate.getFrom().getRank() == fromRank) {
                            matches = true;
                        }
                    }
                    
                    if (matches) {
                        filtered.add(candidate);
                    }
                }
                
                if (!filtered.isEmpty()) {
                    return filtered.get(0);
                }
            }

            // Return first valid candidate
            if (candidates.size() > 0) {
                return candidates.get(0);
            }

            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    /**
     * Resolves a castling move.
     */
    private Move resolveCastling(boolean kingSide) {
        Position kingPos = board.getKingPosition(currentPlayer);
        if (kingPos == null) {
            return null;
        }

        int targetFile = kingSide ? 7 : 0;
        Position rookPos = new Position(targetFile, kingPos.getRank());

        Piece king = board.getPiece(kingPos);
        Piece rook = board.getPiece(rookPos);

        if (king == null || rook == null) {
            return null;
        }

        // Castling target position
        int kingNewFile = kingSide ? 6 : 2;
        Position kingNewPos = new Position(kingNewFile, kingPos.getRank());

        return new Move.Builder(kingPos, kingNewPos, king).isCastling(true).build();
    }

    /**
     * Returns the last move made in standard algebraic notation.
     * This is a simplified version; full SAN generation is complex.
     */
    public String getLastMoveSan() {
        if (moveHistory.isEmpty()) {
            return null;
        }
        Move lastMove = moveHistory.get(moveHistory.size() - 1);
        return lastMove.toString();
    }

    /**
     * Checks if it's white's turn.
     */
    public boolean isWhiteToMove() {
        return currentPlayer == Color.WHITE;
    }

    /**
     * Gets the state (alias for getGameState for compatibility).
     */
    public GameState getState() {
        return gameState;
    }

    /**
     * Sets the state (alias for setGameState for compatibility).
     */
    public void setState(GameState state) {
        setGameState(state);
    }

    /**
     * Gets the winner (for completed games).
     */
    public Player getWinner() {
        if (gameState == GameState.CHECKMATE) {
            // The current player was checkmated, so the opponent wins
            return currentPlayer == Color.WHITE ? blackPlayer : whitePlayer;
        }
        if (gameState == GameState.RESIGNATION) {
            // The current player resigned, so the opponent wins
            return currentPlayer == Color.WHITE ? blackPlayer : whitePlayer;
        }
        return null;
    }

    /**
     * Resigns from the game.
     */
    public void resign() {
            setGameState(GameState.RESIGNATION);
    }

    /**
     * Undoes the last move with full game state restoration.
     * Note: This method doesn't restore board state; use UndoManager for that.
     */
    public void undo() {
        undoLastMove();
    }
    
    /**
     * Resets the game to initial state.
     */
    public void reset() {
        board.reset();
        moveHistory.clear();
        currentPlayer = Color.WHITE;
        gameState = GameState.ONGOING;
        drawOfferPending = false;
        drawOfferer = null;
        clock.reset();
    }

    @Override
    public String toString() {
        return whitePlayer.getName() + " (WHITE) vs " + blackPlayer.getName() + " (BLACK)";
    }
}
