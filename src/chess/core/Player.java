package chess.core;

/**
 * Represents a player in a chess game.
 */
public class Player {
    private final String name;
    private final Color color;
    private final boolean isBot;

    /**
     * Creates a player.
     * 
     * @param name the player's name (must not be empty)
     * @param color the player's color
     * @param isBot true if this is a bot player
     */
    public Player(String name, Color color, boolean isBot) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name must not be empty");
        }
        if (color == null) {
            throw new IllegalArgumentException("Color must not be null");
        }
        
        this.name = name;
        this.color = color;
        this.isBot = isBot;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public boolean isBot() {
        return isBot;
    }

    @Override
    public String toString() {
        return name + " (" + color + ")" + (isBot ? " [BOT]" : "");
    }
}
