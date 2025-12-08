package chess.pgn;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class PgnGameMetadata
{
    private final Map<String, String> tags;

    public PgnGameMetadata() {this.tags = new LinkedHashMap<>();}

    public void setTag(String key, String value)
    {
        Objects.requireNonNull(key, "PGN tag key must not be null");
        Objects.requireNonNull(value, "PGN tag value must not be null");
        if (key.trim().isEmpty()) throw new IllegalArgumentException("PGN tag key must not be empty");
        tags.put(key.trim(), value);
    }

    public String getTag(String key) {return tags.get(key);}

    public boolean hasTag(String key) {return tags.containsKey(key);}

    public Map<String, String> getAllTags() {return Collections.unmodifiableMap(tags);}

    public void setEvent(String event) { setTag("Event", event); }
    public void setSite(String site) { setTag("Site", site); }
    public void setDate(String date) { setTag("Date", date); }
    public void setRound(String round) { setTag("Round", round); }
    public void setWhite(String white) { setTag("White", white); }
    public void setBlack(String black) { setTag("Black", black); }
    public void setResult(String result) { setTag("Result", result); }
}

