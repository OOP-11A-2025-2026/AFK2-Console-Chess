package chess.engine;

import chess.core.Board;
import chess.core.Color;

import java.io.IOException;

/**
 * Generic interface for anything that can produce chess moves (Stockfish, random bot, minimax bot, etc.)
 */
public interface ChessEngine extends AutoCloseable {

    /** Start/init resources (process, threads, etc.) */
    void start() throws IOException;

    /** Optional difficulty controls (implement if supported). */
    default void setSkillLevel(int level) throws IOException { /* optional */ }

    /** Returns best move in UCI format: "e2e4", "g8f6", "a7a8q", etc. */
    String bestMove(Board board, Color sideToMove, int depth) throws IOException;

    /** Stop/cleanup resources */
    void stop() throws IOException;

    @Override
    default void close() throws IOException {
        stop();
    }
}