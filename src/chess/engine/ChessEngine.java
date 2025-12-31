package chess.engine;

import chess.core.Board;
import chess.core.Color;

import java.io.IOException;

/**
 * Generic interface for anything that can produce chess moves (Stockfish, random bot, minimax bot, etc.)
 * Implementations must support UCI protocol communication and position analysis.
 * Classes implementing this interface are responsible for managing their own resources (processes, threads, etc.).
 */
public interface ChessEngine extends AutoCloseable {

    /**
     * Starts the chess engine and initializes resources.
     * Called before any other operations. Implementations should prepare
     * for receiving positions and calculating moves.
     * 
     * @throws IOException if engine startup fails or resources cannot be allocated
     */
    void start() throws IOException;

    /**
     * Sets the difficulty/strength level for the engine (optional method).
     * If not supported by the engine, implementations may safely ignore this call.
     * Typical range is 0-20 where higher values indicate stronger play.
     * 
     * @param level the difficulty level to set (interpretation varies by engine)
     * @throws IOException if communication with engine fails
     */
    default void setSkillLevel(int level) throws IOException { /* optional */ }

    /**
     * Calculates and returns the best move for a given position.
     * The move is returned in UCI format (e.g., "e2e4", "g8f6", "a7a8q" for promotion).
     * The search depth controls how far ahead the engine analyzes.
     * 
     * @param board the current board position
     * @param sideToMove the color of the player to move (WHITE or BLACK)
     * @param depth the search depth in half-moves (plies). Higher = stronger but slower
     * @return the best move in UCI format (e.g., "e2e4")
     * @throws IOException if engine communication fails or move calculation times out
     */
    String bestMove(Board board, Color sideToMove, int depth) throws IOException;

    /**
     * Stops the chess engine and releases all resources.
     * After calling this, the engine should not be used unless start() is called again.
     * 
     * @throws IOException if graceful shutdown fails
     */
    void stop() throws IOException;

    /**
     * Closes the engine resource (AutoCloseable implementation).
     * Calls stop() to ensure proper cleanup. Can be used in try-with-resources statements.
     * 
     * @throws IOException if resource cleanup fails
     */
    @Override
    default void close() throws IOException {
        stop();
    }
}