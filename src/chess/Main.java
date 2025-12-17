package chess;

import chess.core.*;
import chess.io.*;
import chess.rules.*;
import chess.util.*;
import chess.engine.*;
import chess.pgn.*;
import java.io.File;
import java.util.Scanner;

/**
 * Main entry point for the AFK2 Console Chess application.
 * Provides an interactive game loop for playing chess.
 */
public class Main {
    private static Game currentGame;
    private static ConsoleUI ui;
    private static UndoManager undoManager;
    private static CheckDetector checkDetector;
    private static GameController gameController;
    private static Scanner scanner;

    public static void main(String[] args) {
        ui = new ConsoleUI();
        undoManager = new UndoManager();
        checkDetector = new CheckDetector();
        gameController = new GameController();
        scanner = new Scanner(System.in);

        ui.displayWelcome();
        
        // Main game loop
        boolean running = true;
        while (running) {
            if (currentGame == null) {
                showMainMenu();
            } else {
                running = gameLoop();
            }
        }

        ui.displayMessage("Thanks for playing AFK2 Chess!");
        ui.close();
    }

    /**
     * Displays the main menu and handles user input.
     */
    private static void showMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. New Game (2 Players)");
        System.out.println("2. New Game (vs Bot)");
        System.out.println("3. Load Game from PGN");
        System.out.println("4. Help");
        System.out.println("5. Exit");
        System.out.print("\nChoice: ");
        
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                startNewGame();
                break;
            case "2":
                startBotGame();
                break;
            case "3":
                loadGameFromPGN();
                break;
            case "4":
                ui.displayHelp();
                break;
            case "5":
                System.exit(0);
                break;
            default:
                ui.displayError("Invalid choice");
        }
    }

    /**
     * Starts a new game.
     */
    private static void startNewGame() {
        System.out.print("White player name (default: Player 1): ");
        String whiteName = scanner.nextLine().trim();
        if (whiteName.isEmpty()) whiteName = "Player 1";

        System.out.print("Black player name (default: Player 2): ");
        String blackName = scanner.nextLine().trim();
        if (blackName.isEmpty()) blackName = "Player 2";

        Player white = new Player(whiteName, Color.WHITE, false);
        Player black = new Player(blackName, Color.BLACK, false);
        ChessClock clock = new ChessClock(5 * 60 * 1000); // 5 minutes per player

        try {
            currentGame = new Game(white, black, clock);
            undoManager.clear();
            ui.displayMessage("Game started!");
            ui.displayBoard(currentGame.getBoard());
        } catch (Exception e) {
            ui.displayError("Failed to create game: " + e.getMessage());
        }
    }

    /**
     * Starts a new game against the bot.
     */
    private static void startBotGame() {
        System.out.print("Your name (default: Human): ");
        String playerName = scanner.nextLine().trim();
        if (playerName.isEmpty()) playerName = "Human";

        System.out.println("\nBot Difficulty Levels:");
        System.out.println("1. BEGINNER (easiest)");
        System.out.println("2. NOVICE");
        System.out.println("3. INTERMEDIATE");
        System.out.println("4. ADVANCED");
        System.out.println("5. EXPERT");
        System.out.println("6. GRANDMASTER (hardest)");
        System.out.print("\nChoose difficulty (1-6): ");

        String diffChoice = scanner.nextLine().trim();
        BotDifficulty difficulty;

        switch (diffChoice) {
            case "1":
                difficulty = BotDifficulty.BEGINNER;
                break;
            case "2":
                difficulty = BotDifficulty.NOVICE;
                break;
            case "3":
                difficulty = BotDifficulty.INTERMEDIATE;
                break;
            case "4":
                difficulty = BotDifficulty.ADVANCED;
                break;
            case "5":
                difficulty = BotDifficulty.EXPERT;
                break;
            case "6":
                difficulty = BotDifficulty.GRANDMASTER;
                break;
            default:
                difficulty = BotDifficulty.INTERMEDIATE;
                System.out.println("Invalid choice, selecting INTERMEDIATE...");
        }

        System.out.println("\nPlay as White or Black?");
        System.out.println("1. White (you move first)");
        System.out.println("2. Black (bot moves first)");
        System.out.print("Choice: ");

        String colorChoice = scanner.nextLine().trim();
        Color playerColor = colorChoice.equals("2") ? Color.BLACK : Color.WHITE;

        Player human = new Player(playerName, playerColor, false);
        Player bot = new Player("Stockfish " + difficulty.name(), playerColor.opposite(), true);

        Player white = playerColor == Color.WHITE ? human : bot;
        Player black = playerColor == Color.BLACK ? human : bot;

        try {
            ChessClock clock = new ChessClock(5 * 60 * 1000); // 5 minutes per player
            currentGame = new Game(white, black, clock);
            
            // Initialize bot in game controller
            gameController.newGame(white, black, null);
            gameController.initializeBot(difficulty);
            
            undoManager.clear();
            ui.displayMessage("Game started! Playing as " + playerColor.name() + " against " + difficulty.name() + " bot");
            ui.displayBoard(currentGame.getBoard());

            // If bot plays as white, make bot move immediately
            if (bot.getColor() == Color.WHITE) {
                ui.displayMessage("Bot is thinking...");
                handleBotMove();
            }
        } catch (Exception e) {
            ui.displayError("Failed to create bot game: " + e.getMessage());
        }
    }

    /**
     * Loads a game from PGN file.
     */
    private static void loadGameFromPGN() {
        System.out.print("Enter filename to load: ");
        String filename = scanner.nextLine().trim();
        
        if (filename.isEmpty()) {
            ui.displayError("Filename required");
            return;
        }

        try {
            File file = new File(filename);
            if (!file.exists()) {
                ui.displayError("File not found: " + filename);
                return;
            }

            // Use GameController to load the game
            gameController.loadGame(file);
            currentGame = gameController.getGame();
            
            if (currentGame != null) {
                undoManager.clear();
                ui.displayMessage("Game loaded from " + filename);
                ui.displayBoard(currentGame.getBoard());
                ui.displayGameInfo(currentGame);
            } else {
                ui.displayError("Failed to load game");
            }
        } catch (EngineException e) {
            ui.displayError("Failed to load PGN: " + e.getMessage());
        }
    }

    /**
     * Main game loop.
     */
    private static boolean gameLoop() {
        if (currentGame == null) {
            return true;
        }

        // Update game state (check, checkmate, stalemate)
        updateGameState();

        // Display game info and board
        ui.displayGameInfo(currentGame);
        ui.displayBoard(currentGame.getBoard());

        // Display check warning if applicable
        if (currentGame.getGameState() == GameState.CHECK) {
            ui.displayCheck();
        }

        // Display game-ending states
        if (currentGame.getGameState() != GameState.ONGOING && 
            currentGame.getGameState() != GameState.CHECK) {
            ui.displayGameResult(currentGame);
            currentGame = null;
            try {
                gameController.shutdownBot();
            } catch (Exception e) {
                // Ignore
            }
            return true;
        }

        // Check if current player is a bot
        Player currentPlayer = currentGame.getCurrentPlayer();
        if (currentPlayer.isBot() && gameController.isBotInitialized()) {
            return handleBotMove();
        }

        // Get player input
        String input = ui.promptForMove(currentPlayer.getName());

        if (input.isEmpty()) {
            return true;
        }

        // Handle commands and moves
        CommandType cmdType = InputParser.parseCommandType(input);
        if (cmdType != null) {
            return handleCommand(cmdType, input);
        }

        // Try to apply move
        if (InputParser.isMove(input)) {
            return handleMove(input);
        }

        ui.displayError("Unknown input: " + input);
        return true;
    }

    /**
     * Handles a command.
     */
    private static boolean handleCommand(CommandType cmdType, String fullInput) {
        switch (cmdType) {
            case SAVE:
                saveGame(fullInput);
                return true;

            case LOAD:
                handleLoadGame(fullInput);
                return true;

            case UNDO:
                handleUndo();
                return true;

            case RESIGN:
                handleResign();
                return true;

            case DRAW_OFFER:
                handleDrawOffer();
                return true;

            case DRAW_ACCEPT:
                handleDrawAccept();
                return true;

            case HELP:
                ui.displayHelp();
                return true;

            case EXIT:
                if (currentGame != null && ui.promptYesNo("Game in progress. Exit anyway?")) {
                    currentGame = null;
                    return false;
                }
                return currentGame == null ? false : true;

            default:
                ui.displayError("Command not implemented");
                return true;
        }
    }

    /**
     * Handles a move input.
     * Supports: e2e4, e2 e4, Nf3, e4
     */
    private static boolean handleMove(String moveInput) {
        try {
            // Save state for undo
            undoManager.saveSnapshot(currentGame);

            // Check for space-separated coordinate notation (e.g., "e2 e4")
            String[] parts = moveInput.trim().split("\\s+");
            
            if (parts.length == 2 && isSquare(parts[0]) && isSquare(parts[1])) {
                // Space-separated coordinate notation
                try {
                    currentGame.applyMove(parts[0], parts[1]);
                    ui.displayMessage("Move: " + parts[0] + " → " + parts[1]);
                    return true;
                } catch (IllegalArgumentException e) {
                    ui.displayError("Illegal move: " + e.getMessage());
                    undoManager.undo(currentGame);
                    return true;
                }
            }

            // Check for compact coordinate notation (e.g., "e2e4")
            if (moveInput.length() == 4 && moveInput.matches("[a-h][1-8][a-h][1-8]")) {
                String from = moveInput.substring(0, 2);
                String to = moveInput.substring(2, 4);
                try {
                    currentGame.applyMove(from, to);
                    ui.displayMessage("Move: " + from + " → " + to);
                    return true;
                } catch (IllegalArgumentException e) {
                    ui.displayError("Illegal move: " + e.getMessage());
                    undoManager.undo(currentGame);
                    return true;
                }
            }

            // Otherwise try algebraic notation (e.g., "e4", "Nf3", "Bxc5")
            try {
                currentGame.applySan(moveInput);
                ui.displayMessage("Move: " + moveInput);
                return true;
            } catch (IllegalArgumentException e) {
                ui.displayError("Illegal move: " + e.getMessage());
                undoManager.undo(currentGame);
                return true;
            }

        } catch (Exception e) {
            ui.displayError("Move failed: " + e.getMessage());
            undoManager.undo(currentGame);
            return true;
        }
    }

    /**
     * Checks if a string is a valid chess square (e.g., "e4").
     */
    private static boolean isSquare(String str) {
        return str != null && str.length() == 2 && 
               str.charAt(0) >= 'a' && str.charAt(0) <= 'h' &&
               str.charAt(1) >= '1' && str.charAt(1) <= '8';
    }

    /**
     * Handles undo command.
     */
    private static void handleUndo() {
        if (undoManager.undo(currentGame)) {
            ui.displayMessage("Move undone");
        } else {
            ui.displayError("No moves to undo");
        }
    }

    /**
     * Handles resignation.
     */
    private static void handleResign() {
        if (ui.promptYesNo("Are you sure you want to resign?")) {
            Player winner = currentGame.getCurrentPlayer().getColor() == Color.WHITE ?
                    currentGame.getBlackPlayer() : currentGame.getWhitePlayer();
            currentGame.setGameState(GameState.RESIGNATION);
            ui.displayMessage(currentGame.getCurrentPlayer().getName() + " resigned!");
            ui.displayMessage(winner.getName() + " wins!");
        }
    }

    /**
     * Handles load game command during gameplay.
     */
    private static void handleLoadGame(String fullInput) {
        String filename = InputParser.extractFilename(fullInput);
        if (filename == null || filename.isEmpty()) {
            System.out.print("Enter filename to load: ");
            filename = scanner.nextLine().trim();
        }

        if (filename.isEmpty()) {
            ui.displayError("Filename required");
            return;
        }

        try {
            File file = new File(filename);
            if (!file.exists()) {
                ui.displayError("File not found: " + filename);
                return;
            }

            gameController.loadGame(file);
            currentGame = gameController.getGame();
            undoManager.clear();
            ui.displayMessage("Game loaded from " + filename);
            ui.displayBoard(currentGame.getBoard());
        } catch (EngineException e) {
            ui.displayError("Failed to load PGN: " + e.getMessage());
        }
    }

    /**
     * Handles a bot move.
     */
    private static boolean handleBotMove() {
        if (currentGame == null || !gameController.isBotInitialized()) {
            return true;
        }

        try {
            undoManager.saveSnapshot(currentGame);
            ui.displayMessage("Bot is thinking...");

            String uciMove = gameController.getBotMove();

            // Convert UCI move (e.g., "e2e4") to coordinate notation
            String from = uciMove.substring(0, 2);
            String to = uciMove.substring(2, 4);

            // Apply the move
            currentGame.applyMove(from, to);
            ui.displayMessage("Bot played: " + from + " → " + to);

            return true;
        } catch (Exception e) {
            ui.displayError("Bot move failed: " + e.getMessage());
            try {
                undoManager.undo(currentGame);
            } catch (Exception ignored) {
            }
            return true;
        }
    }

    /**
     * Handles draw offer.
     */
    private static void handleDrawOffer() {
        currentGame.offerDraw();
        ui.displayMessage(currentGame.getCurrentPlayer().getName() + " offers a draw");
    }

    /**
     * Handles draw acceptance.
     */
    private static void handleDrawAccept() {
        if (!currentGame.isDrawOfferPending()) {
            ui.displayError("No draw offer to accept");
            return;
        }
        currentGame.acceptDraw();
        ui.displayMessage("Draw accepted!");
    }

    /**
     * Saves a game (placeholder).
     */
    private static void saveGame(String fullInput) {
        String filename = InputParser.extractFilename(fullInput);
        if (filename == null || filename.isEmpty()) {
            System.out.print("Enter filename to save: ");
            filename = scanner.nextLine().trim();
        }
        if (!filename.isEmpty()) {
            ui.displayMessage("Game saved to " + filename);
        }
    }

    /**
     * Updates game state (check, checkmate, stalemate).
     */
    private static void updateGameState() {
        if (currentGame == null) {
            return;
        }

        Color toMove = currentGame.getCurrentPlayerColor();
        Board board = currentGame.getBoard();

        if (checkDetector.isCheckmate(board, toMove)) {
            currentGame.setGameState(GameState.CHECKMATE);
        } else if (checkDetector.isStalemate(board, toMove)) {
            currentGame.setGameState(GameState.STALEMATE);
        } else if (checkDetector.isKingInCheck(board, toMove)) {
            currentGame.setGameState(GameState.CHECK);
        } else {
            currentGame.setGameState(GameState.ONGOING);
        }

        // Check for time out
        if (currentGame.getClock().isFlagFallen(toMove)) {
            currentGame.setGameState(GameState.TIME_OUT);
        }
    }
}
