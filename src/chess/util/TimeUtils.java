package chess.util;

/**
 * Utility class for time-related conversions and formatting.
 */
public class TimeUtils {
    
    private TimeUtils() {
        // Prevent instantiation
    }

    /**
     * Converts milliseconds to a formatted string (MM:SS or MM:SS.mmm).
     */
    public static String formatTime(long milliseconds) {
        if (milliseconds < 0) {
            return "00:00";
        }

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * Converts milliseconds to a formatted string with centiseconds (MM:SS.CC).
     */
    public static String formatTimeWithCentiseconds(long milliseconds) {
        if (milliseconds < 0) {
            return "00:00.00";
        }

        long seconds = milliseconds / 1000;
        long centiseconds = (milliseconds % 1000) / 10;
        long minutes = seconds / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d.%02d", minutes, secs, centiseconds);
    }

    /**
     * Converts seconds to milliseconds.
     */
    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }

    /**
     * Converts minutes to milliseconds.
     */
    public static long minutesToMillis(long minutes) {
        return minutes * 60 * 1000;
    }

    /**
     * Converts milliseconds to seconds.
     */
    public static long millisToSeconds(long milliseconds) {
        return milliseconds / 1000;
    }

    /**
     * Converts milliseconds to minutes.
     */
    public static long millisToMinutes(long milliseconds) {
        return milliseconds / (60 * 1000);
    }

    /**
     * Checks if the given time has expired.
     */
    public static boolean hasExpired(long remainingMillis) {
        return remainingMillis <= 0;
    }

    /**
     * Gets a human-readable description of the time (e.g., "5 minutes", "30 seconds").
     */
    public static String getTimeDescription(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long secs = seconds % 60;

        if (minutes > 0) {
            if (secs > 0) {
                return minutes + " minute" + (minutes > 1 ? "s" : "") + 
                       " and " + secs + " second" + (secs > 1 ? "s" : "");
            } else {
                return minutes + " minute" + (minutes > 1 ? "s" : "");
            }
        } else {
            return secs + " second" + (secs > 1 ? "s" : "");
        }
    }
}
