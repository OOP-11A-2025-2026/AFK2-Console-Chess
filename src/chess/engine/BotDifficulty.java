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

    BotDifficulty(int skillLevel, int thinkingTimeMs) {
        this.skillLevel = skillLevel;
        this.thinkingTimeMs = thinkingTimeMs;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getThinkingTimeMs() {
        return thinkingTimeMs;
    }

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
