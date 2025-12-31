package chess.io;

import chess.core.*;
import chess.util.BoardPrinter;
import java.util.Scanner;

/**
 * Handles all console-based user interface operations.
 * Manages board printing, user prompts, error messages, and result screens.
 */
public class ConsoleUI {

    private final Scanner scanner;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    /**
     * Creates a ConsoleUI with a default Scanner reading from System.in.
     */
    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Creates a ConsoleUI with a custom Scanner (useful for testing).
     */
    public ConsoleUI(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Prints the chess board to the console.
     * 
     * @param board the board to print
     */
    public void displayBoard(Board board) {
        if (board == null) {
            System.out.println(ANSI_RED + "Error: Board is null" + ANSI_RESET);
            return;
        }
        BoardPrinter.printBoard(board);
    }

    /**
     * Prints a welcome message.
     */
    public void displayWelcome() {
        System.out.println(ANSI_BLUE + "=== Welcome to AFK2 Console Chess ===" + ANSI_RESET);
        System.out.println("Type 'help' for available commands.");
        System.out.println();
    }

    /**
     * Displays game information.
     * 
     * @param game the current game
     */
    public void displayGameInfo(Game game) {
        if (game == null) {
            return;
        }

        System.out.println(ANSI_BLUE + "--- Game Info ---" + ANSI_RESET);
        System.out.println("White: " + game.getWhitePlayer().getName());
        System.out.println("Black: " + game.getBlackPlayer().getName());
        System.out.println("Current Player: " + game.getCurrentPlayer().getName());
        System.out.println("Move Count: " + game.getMoveHistory().size());
        
        if (game.isDrawOfferPending()) {
            System.out.println(ANSI_YELLOW + "Draw offered by " + game.getDrawOfferer() + ANSI_RESET);
        }
        
        System.out.println();
    }

    /**
     * Prompts the current player for a move.
     * 
     * @param playerName the name of the player to move
     * @return the user's input
     */
    public String promptForMove(String playerName) {
        System.out.print(ANSI_GREEN + playerName + "'s move: " + ANSI_RESET);
        return scanner.nextLine().trim();
    }

    /**
     * Prompts the user to accept or decline a draw offer.
     * 
     * @return true if user accepts, false otherwise
     */
    public boolean promptForDrawResponse() {
        System.out.print(ANSI_YELLOW + "Accept draw? (yes/no): " + ANSI_RESET);
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }

    /**
     * Prompts the user for a pawn promotion choice.
     * 
     * @return the promotion character (Q, R, B, N)
     */
    public char promptForPromotion() {
        while (true) {
            System.out.print(ANSI_YELLOW + "Choose promotion (Q/R/B/N): " + ANSI_RESET);
            String input = scanner.nextLine().trim().toUpperCase();
            
            if (input.length() == 1) {
                char ch = input.charAt(0);
                if ("QRBN".indexOf(ch) >= 0) {
                    return ch;
                }
            }
            
            displayError("Invalid choice. Please enter Q, R, B, or N.");
        }
    }

    /**
     * Prompts for a filename for save/load operations.
     * 
     * @param operation the operation name (e.g., "save" or "load")
     * @return the filename entered by the user
     */
    public String promptForFilename(String operation) {
        System.out.print(ANSI_BLUE + "Enter filename for " + operation + ": " + ANSI_RESET);
        return scanner.nextLine().trim();
    }

    /**
     * Displays an error message.
     * 
     * @param message the error message
     */
    public void displayError(String message) {
        System.out.println(ANSI_RED + "Error: " + message + ANSI_RESET);
    }

    /**
     * Displays an informational message.
     * 
     * @param message the message
     */
    public void displayMessage(String message) {
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }

    /**
     * Displays a warning message.
     * 
     * @param message the warning message
     */
    public void displayWarning(String message) {
        System.out.println(ANSI_YELLOW + "Warning: " + message + ANSI_RESET);
    }

    /**
     * Displays the result of a completed game.
     * 
     * @param game the completed game
     */
    public void displayGameResult(Game game) {
        if (game == null) {
            return;
        }

        System.out.println("\n" + ANSI_BLUE + "=== Game Over ===" + ANSI_RESET);
        
        GameState state = game.getGameState();
        
        switch (state) {
            case CHECKMATE:
                Color loser = game.getCurrentPlayerColor();
                Color winner = loser.opposite();
                Player winnerPlayer = winner == Color.WHITE ? 
                    game.getWhitePlayer() : game.getBlackPlayer();
                System.out.println(ANSI_GREEN + "Checkmate! " + winnerPlayer.getName() + " wins!" + ANSI_RESET);
                break;
                
            case STALEMATE:
                System.out.println(ANSI_YELLOW + "Stalemate! Draw." + ANSI_RESET);
                break;
                
            case DRAW_BY_AGREEMENT:
                System.out.println(ANSI_YELLOW + "Draw by agreement." + ANSI_RESET);
                break;
                
            case RESIGNATION:
                Player resignedPlayer = game.getCurrentPlayer();
                Player other = resignedPlayer.getColor() == Color.WHITE ? 
                    game.getBlackPlayer() : game.getWhitePlayer();
                System.out.println(ANSI_GREEN + resignedPlayer.getName() + " resigned. " + 
                                 other.getName() + " wins!" + ANSI_RESET);
                break;
                
            case TIME_OUT:
                Player timedOut = game.getCurrentPlayer();
                Player other2 = timedOut.getColor() == Color.WHITE ? 
                    game.getBlackPlayer() : game.getWhitePlayer();
                System.out.println(ANSI_GREEN + timedOut.getName() + " is out of time. " + 
                                 other2.getName() + " wins!" + ANSI_RESET);
                break;
                
            default:
                System.out.println("Game ended: " + state);
        }
        
        System.out.println("Total moves: " + game.getMoveHistory().size());
        System.out.println();
    }

    /**
     * Displays check notification to alert the player.
     * Shows prominent visual warning that the king is in check.
     */
    public void displayCheck() {
        System.out.println(ANSI_RED + "*** Check! ***" + ANSI_RESET);
    }

    /**
     * Displays the help menu with all available commands and move formats.
     * Shows command descriptions and examples of different notation styles.
     */
    public void displayHelp() {
        System.out.println("\n" + ANSI_BLUE + "=== Available Commands ===" + ANSI_RESET);
        
        for (CommandType type : CommandType.values()) {
            System.out.println(ANSI_GREEN + type.getCommand() + ANSI_RESET + 
                             " - " + type.getDescription());
        }
        
        System.out.println("\n" + ANSI_BLUE + "=== Move Formats ===" + ANSI_RESET);
        System.out.println("Coordinate: e2e4 (move piece from e2 to e4)");
        System.out.println("Algebraic:  e4 (move pawn to e4)");
        System.out.println("            Nf3 (move knight to f3)");
        System.out.println("            Bxc5 (bishop captures on c5)");
        System.out.println("            O-O (king-side castling)");
        System.out.println("            O-O-O (queen-side castling)");
        System.out.println();
    }

    /**
     * Displays a move that was made in green color.
     * Shows the player name and the move in algebraic notation.
     * 
     * @param move the move that was made
     * @param player the player who made the move
     */
    public void displayMove(Move move, Player player) {
        if (move == null || player == null) {
            return;
        }

        System.out.println(ANSI_GREEN + player.getName() + ": " + move + ANSI_RESET);
    }

    /**
     * Clears the screen (platform-dependent implementation).
     * Uses ANSI escape codes on Unix/macOS and cls command on Windows.
     * Falls back to printing newlines if platform detection fails.
     */
    public void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\u001b[2J\u001b[H");
            }
        } catch (Exception e) {
            // If clearing fails, just print newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Closes the scanner and releases all I/O resources.
     * Should be called when the UI is no longer needed to prevent resource leaks.
     */
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * Prompts yes/no question and returns the response.
     * Accepts "yes", "y" (case-insensitive) as affirmative responses.
     * 
     * @param prompt the question to ask the user
     * @return true for yes/y, false for any other response
     */
    public boolean promptYesNo(String prompt) {
        System.out.print(ANSI_BLUE + prompt + " (yes/no): " + ANSI_RESET);
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }

    /**
     * Displays the complete move history of the game.
     * Formats moves in pairs (white/black) with move numbers.
     * 
     * @param game the game whose move history should be displayed
     */
    public void displayMoveHistory(Game game) {
        if (game == null) {
            return;
        }

        System.out.println(ANSI_BLUE + "=== Move History ===" + ANSI_RESET);
        int moveNum = 1;
        for (int i = 0; i < game.getMoveHistory().size(); i++) {
            Move move = game.getMoveHistory().get(i);
            if (i % 2 == 0) {
                System.out.print(moveNum + ". " + move + " ");
            } else {
                System.out.println(move);
                moveNum++;
            }
        }
        System.out.println();
    }

    /**
     * Displays undo availability hint to the player.
     * Shows a helpful tip only when undo is available.
     * 
     * @param canUndo whether undo is available for the current game state
     */
    public void displayUndoHint(boolean canUndo) {
        if (canUndo) {
            System.out.println(ANSI_GREEN + "💡 Tip: Type 'undo' to undo your last move" + ANSI_RESET);
        }
    }

    /**
     * Displays successful undo confirmation message.
     * Shows checkmark and confirmation that the last move was successfully undone.
     */
    public void displayUndoSuccess() {
        System.out.println(ANSI_GREEN + "✓ Move successfully undone" + ANSI_RESET);
    }

    /**
     * Displays message when undo is attempted but unavailable.
     * Shows error indicating there are no moves to undo.
     */
    public void displayUndoUnavailable() {
        System.out.println(ANSI_RED + "✗ No moves to undo" + ANSI_RESET);
    }
}

