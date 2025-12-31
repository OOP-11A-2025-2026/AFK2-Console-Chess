package chess.engine;

/**
 * Enum representing bot difficulty levels for AI players.
 * Maps to Stockfish skill levels or thinking depth.
 */
public enum BotDifficulty {
    BEGINNER(1, 100),           // Skill 0-2, depth ~5
    NOVICE(3, 200),             // Skill 3-5, depth ~8
    INTERMEDIATE(6, 300),       // Skill 6-10, depth ~12
    ADVANCED(15, 500),          // Skill 15-18, depth ~18
    EXPERT(19, 1000),           // Skill 19+, depth ~25
    GRANDMASTER(20, 2000);      // Skill 20 (max), depth ~35+

    private final int skillLevel;
    private final int thinkingTimeMs;

    /**
     * Creates a BotDifficulty enum constant.
     * Associates a Stockfish skill level and thinking time with a difficulty tier.
     * 
     * @param skillLevel the Stockfish skill level (0-20 range)
     * @param thinkingTimeMs the recommended thinking time in milliseconds
     */
    BotDifficulty(int skillLevel, int thinkingTimeMs) {
        this.skillLevel = skillLevel;
        this.thinkingTimeMs = thinkingTimeMs;
    }

    /**
     * Gets the Stockfish skill level for this difficulty.
     * Skill levels range from 0 (weakest) to 20 (strongest).
     * 
     * @return the Stockfish skill level (0-20)
     */
    public int getSkillLevel() {
        return skillLevel;
    }

    /**
     * Gets the recommended thinking time for this difficulty.
     * This is used as a hint for time management in the game.
     * 
     * @return the thinking time in milliseconds
     */
    public int getThinkingTimeMs() {
        return thinkingTimeMs;
    }

    /**
     * Gets the recommended search depth for this difficulty.
     * Higher difficulties use deeper searches for stronger play.
     * Depth is measured in half-moves (plies) for the engine search.
     * 
     * @return the recommended search depth
     */
    public int getSearchDepth() {
        switch (this) {
            case BEGINNER:
                return 5;
            case NOVICE:
                return 8;
            case INTERMEDIATE:
                return 12;
            case ADVANCED:
                return 18;
            case EXPERT:
                return 25;
            case GRANDMASTER:
                return 35;
            default:
                return 5;
        }
    }

    /**
     * Returns a user-friendly description of the difficulty.
     */
    public String getDescription() {
        switch (this) {
            case BEGINNER:
                return "Very easy - novice player";
            case NOVICE:
                return "Easy - casual player";
            case INTERMEDIATE:
                return "Medium - club player";
            case ADVANCED:
                return "Hard - strong player";
            case EXPERT:
                return "Very hard - expert player";
            case GRANDMASTER:
                return "Insane - grandmaster level";
            default:
                return "Unknown";
        }
    }
}
