package chess.engine;

/**
 * Represents time control settings for a chess game.
 * Defines initial time and optional increment per move.
 */
public class TimeControl {
    private final long initialTimeMs;
    private final long incrementMs;

    /**
     * Creates a time control with initial time and increment.
     * 
     * @param initialTimeMs the initial time in milliseconds for each player
     * @param incrementMs the time increment per move in milliseconds
     */
    public TimeControl(long initialTimeMs, long incrementMs) {
        if (initialTimeMs <= 0) {
            throw new IllegalArgumentException("Initial time must be positive");
        }
        if (incrementMs < 0) {
            throw new IllegalArgumentException("Increment cannot be negative");
        }
        this.initialTimeMs = initialTimeMs;
        this.incrementMs = incrementMs;
    }

    /**
     * Creates a time control with no increment.
     * 
     * @param initialTimeMs the initial time in milliseconds for each player
     */
    public TimeControl(long initialTimeMs) {
        this(initialTimeMs, 0);
    }

    public long getInitialTimeMs() {
        return initialTimeMs;
    }

    public long getIncrementMs() {
        return incrementMs;
    }

    /**
     * Creates common time control presets.
     */
    public static TimeControl blitzGame() {
        return new TimeControl(5 * 60 * 1000, 0);  // 5 minutes
    }

    public static TimeControl rapidGame() {
        return new TimeControl(10 * 60 * 1000, 5 * 1000);  // 10 minutes + 5 second increment
    }

    public static TimeControl classicalGame() {
        return new TimeControl(90 * 60 * 1000, 30 * 1000);  // 90 minutes + 30 second increment
    }

    @Override
    public String toString() {
        if (incrementMs > 0) {
            return String.format("%d+%d", initialTimeMs / 60000, incrementMs / 1000);
        }
        return String.format("%d", initialTimeMs / 60000);
    }
}
