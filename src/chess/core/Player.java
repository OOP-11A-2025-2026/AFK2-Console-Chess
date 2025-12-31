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

    /**
     * Gets the player's name.
     * 
     * @return the player's name (non-empty string)
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the player's color in this game.
     * 
     * @return the player's color (WHITE or BLACK)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Checks if this player is a bot AI player.
     * 
     * @return true if this is a bot/AI player, false if human
     */
    public boolean isBot() {
        return isBot;
    }

    /**
     * Returns a string representation of the player.
     * Format: "Name (COLOR)" or "Name (COLOR) [BOT]" if it's a bot.
     * 
     * @return a formatted string describing the player
     */
    @Override
    public String toString() {
        return name + " (" + color + ")" + (isBot ? " [BOT]" : "");
    }
}
