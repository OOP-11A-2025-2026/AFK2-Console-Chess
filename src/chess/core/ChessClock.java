package chess.core;

/**
 * Represents a chess clock for tracking per-player time.
 */
public class ChessClock {
    private final long initialTimeMillis;
    private long whiteRemainingMillis;
    private long blackRemainingMillis;
    
    private long lastStartTime;
    private Color currentPlayer;

    /**
     * Creates a chess clock with initial time.
     * 
     * @param initialTimeMillis the initial time in milliseconds for each player
     */
    public ChessClock(long initialTimeMillis) {
        if (initialTimeMillis <= 0) {
            throw new IllegalArgumentException("Initial time must be positive");
        }
        this.initialTimeMillis = initialTimeMillis;
        this.whiteRemainingMillis = initialTimeMillis;
        this.blackRemainingMillis = initialTimeMillis;
        this.currentPlayer = null;
        this.lastStartTime = 0;
    }

    /**
     * Starts the clock for the given player.
     */
    public void startTurn(Color player) {
        if (player == null) {
            throw new IllegalArgumentException("Player color must not be null");
        }
        this.currentPlayer = player;
        this.lastStartTime = System.currentTimeMillis();
    }

    /**
     * Stops the clock for the current player.
     */
    public void stopTurn() {
        if (currentPlayer == null) {
            return;
        }

        long elapsedMillis = System.currentTimeMillis() - lastStartTime;

        if (currentPlayer == Color.WHITE) {
            whiteRemainingMillis -= elapsedMillis;
            if (whiteRemainingMillis < 0) {
                whiteRemainingMillis = 0;
            }
        } else {
            blackRemainingMillis -= elapsedMillis;
            if (blackRemainingMillis < 0) {
                blackRemainingMillis = 0;
            }
        }

        currentPlayer = null;
        lastStartTime = 0;
    }

    /**
     * Gets the remaining time for a player in milliseconds.
     */
    public long getRemainingTime(Color player) {
        if (player == null) {
            throw new IllegalArgumentException("Player color must not be null");
        }

        long remaining = player == Color.WHITE ? whiteRemainingMillis : blackRemainingMillis;

        // If the clock is running for this player, subtract elapsed time
        if (currentPlayer == player) {
            long elapsedMillis = System.currentTimeMillis() - lastStartTime;
            remaining -= elapsedMillis;
        }

        return Math.max(0, remaining);
    }

    /**
     * Checks if the flag has fallen for a player.
     */
    public boolean isFlagFallen(Color player) {
        return getRemainingTime(player) <= 0;
    }

    /**
     * Resets the clock to initial values.
     */
    public void reset() {
        this.whiteRemainingMillis = initialTimeMillis;
        this.blackRemainingMillis = initialTimeMillis;
        this.currentPlayer = null;
        this.lastStartTime = 0;
    }

    /**
     * Gets remaining time for white in seconds.
     */
    public long getWhiteRemainingSeconds() {
        return getRemainingTime(Color.WHITE) / 1000;
    }

    /**
     * Gets remaining time for black in seconds.
     */
    public long getBlackRemainingSeconds() {
        return getRemainingTime(Color.BLACK) / 1000;
    }
}
