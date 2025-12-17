package chess.rules;

import chess.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages undo functionality by storing snapshots of the entire game state.
 * Supports undoing moves consistently.
 */
public class UndoManager {

        /**
         * Represents a snapshot of the game state at a specific point in time.
         */
        private static class GameSnapshot {
            private final Board boardState;
            private final GameState gameState;
            private final List<Move> moveHistory;

            public GameSnapshot(Game game) {
                this.boardState = game.getBoard().copy();
                this.gameState = game.getGameState();
                this.moveHistory = new ArrayList<>(game.getMoveHistory());
            }

            public void restoreToGame(Game game) {
                // Clear and restore board
                game.getBoard().clear();
                for (int file = 0; file < 8; file++) {
                    for (int rank = 0; rank < 8; rank++) {
                        Position pos = new Position(file, rank);
                        Piece piece = boardState.getPiece(pos);
                        if (piece != null) {
                            game.getBoard().placePiece(piece.copy(pos), pos);
                        }
                    }
                }

                // Restore game state
                game.setGameState(gameState);
                game.getMoveHistory().clear();
                game.getMoveHistory().addAll(moveHistory);
            }
        }    private final List<GameSnapshot> snapshots;
    private final int maxSnapshots;

    /**
     * Creates an UndoManager with a maximum number of snapshots.
     * 
     * @param maxSnapshots the maximum number of snapshots to keep (0 = unlimited)
     */
    public UndoManager(int maxSnapshots) {
        this.snapshots = new ArrayList<>();
        this.maxSnapshots = maxSnapshots;
    }

    /**
     * Creates an UndoManager with unlimited snapshots.
     */
    public UndoManager() {
        this(0);
    }

    /**
     * Saves a snapshot of the current game state.
     * 
     * @param game the game to snapshot
     */
    public void saveSnapshot(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game must not be null");
        }

        snapshots.add(new GameSnapshot(game));

        // Remove old snapshots if limit is exceeded
        if (maxSnapshots > 0 && snapshots.size() > maxSnapshots) {
            snapshots.remove(0);
        }
    }

    /**
     * Undoes the last move by restoring the previous snapshot.
     * 
     * @param game the game to undo
     * @return true if undo was successful, false if no snapshots available
     */
    public boolean undo(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game must not be null");
        }

        if (snapshots.isEmpty()) {
            return false;
        }

        GameSnapshot snapshot = snapshots.remove(snapshots.size() - 1);
        snapshot.restoreToGame(game);
        return true;
    }

    /**
     * Checks if an undo is available.
     * 
     * @return true if at least one snapshot exists
     */
    public boolean canUndo() {
        return !snapshots.isEmpty();
    }

    /**
     * Gets the number of available snapshots.
     * 
     * @return the number of snapshots
     */
    public int getSnapshotCount() {
        return snapshots.size();
    }

    /**
     * Clears all snapshots.
     */
    public void clear() {
        snapshots.clear();
    }

    /**
     * Resets the undo manager (same as clear).
     */
    public void reset() {
        clear();
    }
}

