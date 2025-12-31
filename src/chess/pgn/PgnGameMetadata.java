package chess.pgn;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents metadata for a chess game in PGN format.
 * Stores standard PGN tags like Event, Site, Date, Round, White, Black, and Result.
 */
public final class PgnGameMetadata {
    private final Map<String, String> tags;

    /**
     * Creates an empty PgnGameMetadata instance.
     */
    public PgnGameMetadata() {
        this.tags = new LinkedHashMap<>();
    }

    /**
     * Sets a PGN tag with a key-value pair.
     * 
     * @param key the tag key (e.g., "Event", "Site", "Date")
     * @param value the tag value
     * @throws IllegalArgumentException if key is null or empty
     * @throws NullPointerException if value is null
     */
    public void setTag(String key, String value) {
        Objects.requireNonNull(key, "PGN tag key must not be null");
        Objects.requireNonNull(value, "PGN tag value must not be null");
        if (key.trim().isEmpty()) throw new IllegalArgumentException("PGN tag key must not be empty");
        tags.put(key.trim(), value);
    }

    /**
     * Gets a PGN tag value by key.
     * 
     * @param key the tag key
     * @return the tag value, or null if not found
     */
    public String getTag(String key) {
        return tags.get(key);
    }

    /**
     * Checks if a PGN tag exists.
     * 
     * @param key the tag key
     * @return true if the tag exists, false otherwise
     */
    public boolean hasTag(String key) {
        return tags.containsKey(key);
    }

    /**
     * Gets all PGN tags as an unmodifiable map.
     * 
     * @return an unmodifiable map of all tags
     */
    public Map<String, String> getAllTags() {
        return Collections.unmodifiableMap(tags);
    }

    /**
     * Sets the Event tag.
     * 
     * @param event the event name
     */
    public void setEvent(String event) { 
        setTag("Event", event); 
    }
    
    /**
     * Sets the Site tag.
     * 
     * @param site the site/location
     */
    public void setSite(String site) { 
        setTag("Site", site); 
    }
    
    /**
     * Sets the Date tag.
     * 
     * @param date the date in YYYY.MM.DD format
     */
    public void setDate(String date) { 
        setTag("Date", date); 
    }
    
    /**
     * Sets the Round tag.
     * 
     * @param round the round number or designation
     */
    public void setRound(String round) { 
        setTag("Round", round); 
    }
    
    /**
     * Sets the White player tag.
     * 
     * @param white the white player's name
     */
    public void setWhite(String white) { 
        setTag("White", white); 
    }
    
    /**
     * Sets the Black player tag.
     * 
     * @param black the black player's name
     */
    public void setBlack(String black) { 
        setTag("Black", black); 
    }
    
    /**
     * Sets the Result tag.
     * 
     * @param result the game result (e.g., "1-0", "0-1", "1/2-1/2", "*")
     */
    public void setResult(String result) { 
        setTag("Result", result); 
    }
}

