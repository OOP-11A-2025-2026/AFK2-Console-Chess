package chess.core;

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
