package chess.io;

/**
 * Enum representing the types of commands that can be entered by the player.
 */
public enum CommandType {
    MOVE("move", "Make a move (coordinate: e2e4 or algebraic: e4)"),
    NEW_GAME("new", "Start a new game"),
    LOAD("load", "Load a game from file"),
    SAVE("save", "Save the current game"),
    RESIGN("resign", "Resign from the game"),
    DRAW_OFFER("draw", "Offer a draw"),
    DRAW_ACCEPT("accept", "Accept a draw offer"),
    UNDO("undo", "Undo the last move"),
    HELP("help", "Show help information"),
    EXIT("exit", "Exit the program");

    private final String command;
    private final String description;

    CommandType(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Parses a command string to a CommandType.
     * Returns null if the string doesn't match any command.
     * 
     * @param input the input string (case-insensitive)
     * @return the CommandType, or null if no match
     */
    public static CommandType parseCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String command = input.trim().toLowerCase();

        for (CommandType type : CommandType.values()) {
            if (command.equals(type.command)) {
                return type;
            }
        }

        return null;
    }

    /**
     * Checks if a string is a recognized command.
     * 
     * @param input the input string
     * @return true if it matches a command
     */
    public static boolean isCommand(String input) {
        return parseCommand(input) != null;
    }

    /**
     * Checks if a string appears to be a move (not a command).
     * Moves typically contain coordinates like "e2e4" or algebraic notation like "e4".
     * 
     * @param input the input string
     * @return true if it looks like a move
     */
    public static boolean isMove(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // If it's a recognized command, it's not a move
        if (isCommand(input)) {
            return false;
        }

        String trimmed = input.trim().toLowerCase();

        // Coordinate notation: e2e4 (4 characters, all letters/numbers)
        if (trimmed.length() == 4) {
            return trimmed.matches("[a-h][1-8][a-h][1-8]");
        }

        // Algebraic notation: e4, Nf3, Bxc5, O-O (castling), etc.
        // Can be 1-5 characters long
        if (trimmed.length() >= 2 && trimmed.length() <= 5) {
            // Check if it starts with a piece letter or a file letter
            char first = trimmed.charAt(0);
            if (Character.isLetter(first)) {
                return true;
            }
        }

        return false;
    }
}

