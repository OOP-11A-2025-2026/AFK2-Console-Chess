package chess.core;

/**
 * Enum representing the state of a chess game.
 */
public enum GameState {
    ONGOING,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_BY_AGREEMENT,
    RESIGNATION,
    TIME_OUT
}
