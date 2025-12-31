package chess.util;

/**
 * Utility class for time-related conversions and formatting.
 * Provides methods to convert between time units and format time for display.
 */
public class TimeUtils {
    
    private TimeUtils() {
        // Prevent instantiation
    }

    /**
     * Converts milliseconds to a formatted string (MM:SS).
     * 
     * @param milliseconds the time in milliseconds
     * @return formatted time string (e.g., "05:30")
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
     * 
     * @param milliseconds the time in milliseconds
     * @return formatted time string with centiseconds (e.g., "05:30.45")
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
     * 
     * @param seconds the time in seconds
     * @return the time in milliseconds
     */
    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }

    /**
     * Converts minutes to milliseconds.
     * 
     * @param minutes the time in minutes
     * @return the time in milliseconds
     */
    public static long minutesToMillis(long minutes) {
        return minutes * 60 * 1000;
    }

    /**
     * Converts milliseconds to seconds.
     * 
     * @param milliseconds the time in milliseconds
     * @return the time in seconds
     */
    public static long millisToSeconds(long milliseconds) {
        return milliseconds / 1000;
    }

    /**
     * Converts milliseconds to minutes.
     * 
     * @param milliseconds the time in milliseconds
     * @return the time in minutes
     */
    public static long millisToMinutes(long milliseconds) {
        return milliseconds / (60 * 1000);
    }

    /**
     * Checks if the given time has expired (reached zero or below).
     * 
     * @param remainingMillis the remaining time in milliseconds
     * @return true if time has expired, false otherwise
     */
    public static boolean hasExpired(long remainingMillis) {
        return remainingMillis <= 0;
    }

    /**
     * Gets a human-readable description of the time (e.g., "5 minutes", "30 seconds").
     * 
     * @param milliseconds the time in milliseconds
     * @return a human-readable time description
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
