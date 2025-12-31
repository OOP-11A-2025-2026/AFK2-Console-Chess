package chess;

import chess.core.*;
import chess.io.*;
import chess.rules.*;
import chess.util.*;
import chess.engine.*;
import chess.pgn.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.List;
import java.util.Random;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    private static CommandHandler commandHandler;
    private static Scanner scanner;
    private static Random random;

    public static void main(String[] args) {
        ui = new ConsoleUI();
        undoManager = new UndoManager();
        checkDetector = new CheckDetector();
        gameController = new GameController();
        commandHandler = new CommandHandler(gameController, ui);
        scanner = new Scanner(System.in);
        random = new Random();

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

        try {
            gameController.setupNewGame(whiteName, blackName);
            currentGame = gameController.getGame();
            undoManager.clear();
            undoManager.saveSnapshot(currentGame);  // Save initial board state
            ui.displayMessage("Game started!");
            ui.displayBoard(currentGame.getBoard());
        } catch (EngineException e) {
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

        try {
            boolean botPlayFirst = gameController.setupBotGame(playerName, difficulty, playerColor);
            currentGame = gameController.getGame();
            
            undoManager.clear();
            undoManager.saveSnapshot(currentGame);  // Save initial board state
            ui.displayMessage("Bot initialized (" + difficulty.name() + ")");
            ui.displayMessage("Game started! Playing as " + playerColor.name() + " against " + difficulty.name() + " bot");
            ui.displayBoard(currentGame.getBoard());

            // If bot plays as white, make bot move immediately
            if (botPlayFirst) {
                ui.displayMessage("Bot is thinking... (may take a few seconds)");
                handleBotMove();
            }
        } catch (EngineException e) {
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
                undoManager.saveSnapshot(currentGame);  // Save initial board state
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

        // Display game info (and board unless a draw response is pending)
        ui.displayGameInfo(currentGame);
        if (!currentGame.isDrawOfferPending()) {
            ui.displayBoard(currentGame.getBoard());
        } else {
            ui.displayMessage("(Draw pending — board hidden. Type 'accept' or 'reject', or play a move to decline.)");
        }

        // Display check warning if applicable
        if (currentGame.getGameState() == GameState.CHECK) {
            ui.displayCheck();
        }

        // Display undo hint if moves are available (10% random chance)
        if (undoManager.canUndo() && random.nextInt(100) < 10) {
            ui.displayUndoHint(true);
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

            if (parts.length == 2 && AlgebraicNotationUtil.isValidSquare(parts[0]) && AlgebraicNotationUtil.isValidSquare(parts[1])) {
                String from = parts[0];
                String to = parts[1];

                // If this is castling in coordinate form (e1 g1, e1 c1, e8 g8, e8 c8),
                // route it through SAN so the rook is moved by the engine.
                String castleSan = AlgebraicNotationUtil.convertCastlingCoordinateToSan(from, to);
                try {
                    if (castleSan != null) {
                        currentGame.applySan(castleSan);
                        ui.displayMessage("Move: " + castleSan);
                    } else {
                        currentGame.applyMove(from, to);
                        ui.displayMessage("Move: " + from + " → " + to);
                    }
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

                String castleSan = AlgebraicNotationUtil.convertCastlingCoordinateToSan(from, to);
                try {
                    if (castleSan != null) {
                        currentGame.applySan(castleSan);
                        ui.displayMessage("Move: " + castleSan);
                    } else {
                        currentGame.applyMove(from, to);
                        ui.displayMessage("Move: " + from + " → " + to);
                    }
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
     * Handles undo command.
     * Undoes both the opponent's last move and the player's own last move.
     */
    private static void handleUndo() {
        if (undoManager.undoFullTurn(currentGame)) {
            ui.displayUndoSuccess();
            ui.displayBoard(currentGame.getBoard());
            ui.displayGameInfo(currentGame);
        } else {
            ui.displayUndoUnavailable();
        }
    }

    /**
     * Handles resignation.
     */
    private static void handleResign() {
        if (ui.promptYesNo("Are you sure you want to resign?")) {
            Player winner = currentGame.getCurrentPlayer().getColor() == Color.WHITE ?
                    currentGame.getBlackPlayer() : currentGame.getWhitePlayer();
            currentGame.resign();
            ui.displayMessage(currentGame.getCurrentPlayer().getName() + " resigned!");
            ui.displayMessage(winner.getName() + " wins!");
            currentGame = null;
            try {
                gameController.shutdownBot();
            } catch (Exception ignored) {
            }
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
        try {
            return commandHandler.handleBotMove(gameController, currentGame, undoManager);
        } catch (EngineException e) {
            ui.displayError("Bot move failed: " + e.getMessage());
            return true;
        }
    }

    /**
     * Handles draw offer.
     */
    private static void handleDrawOffer() {
        if (currentGame.isDrawOfferPending()) {
            ui.displayError("Draw offer already pending — accept, reject, or play a move.");
            return;
        }
        currentGame.offerDraw();
        ui.displayMessage(currentGame.getCurrentPlayer().getName() + " offers a draw");

        currentGame.switchPlayer(); // opponent responds
    }

    private static void handleDrawAccept() {
        if (!currentGame.isDrawOfferPending()) {
            ui.displayError("No draw offer to accept");
            return;
        }
        currentGame.acceptDraw();
        ui.displayMessage("Draw accepted!");
        ui.displayGameResult(currentGame);
    }

    private static void handleDrawReject() {
        if (!currentGame.isDrawOfferPending()) {
            ui.displayError("No draw offer to reject");
            return;
        }
        currentGame.clearDrawOffer();
        ui.displayMessage("Draw offer rejected.");
        // keep turn with current player (they already had next move)
    }

    /**
     * Saves a game to a PGN file.
     * Validates game state, creates metadata, converts moves to SAN, and writes to file.
     */
    private static void saveGame(String fullInput) {
        // Validate game state
        if (currentGame == null) {
            ui.displayError("No game to save");
            return;
        }

        // Extract filename
        String filename = InputParser.extractFilename(fullInput);
        if (filename == null || filename.isEmpty()) {
            System.out.print("Enter filename to save: ");
            filename = scanner.nextLine().trim();
        }

        if (filename.isEmpty()) {
            ui.displayError("Filename required");
            return;
        }

        try {
            // Ensure .pgn extension
            if (!filename.toLowerCase().endsWith(".pgn")) {
                filename += ".pgn";
            }

            // Create PGN metadata
            PgnGameMetadata metadata = new PgnGameMetadata();
            metadata.setEvent("Casual Game");
            metadata.setSite("AFK2 Chess");
            metadata.setDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            metadata.setRound("1");
            metadata.setWhite(currentGame.getWhitePlayer().getName());
            metadata.setBlack(currentGame.getBlackPlayer().getName());

            // Determine result
            String result = "?";
            switch (currentGame.getGameState()) {
                case CHECKMATE:
                    result = currentGame.getCurrentPlayerColor() == Color.BLACK ? "1-0" : "0-1";
                    break;
                case STALEMATE:
                    result = "1/2-1/2";
                    break;
                case DRAW_BY_AGREEMENT:
                    result = "1/2-1/2";
                    break;
                case RESIGNATION:
                    result = currentGame.getCurrentPlayerColor() == Color.WHITE ? "0-1" : "1-0";
                    break;
                case TIME_OUT:
                    result = currentGame.getCurrentPlayerColor() == Color.WHITE ? "0-1" : "1-0";
                    break;
                case ONGOING:
                case CHECK:
                    result = "*";
                    break;
            }
            metadata.setResult(result);

            // Convert move history to PGN move records
            List<Move> moveHistory = currentGame.getMoveHistory();
            List<PgnMoveRecord> pgnMoves = new java.util.ArrayList<>();

            for (int i = 0; i < moveHistory.size(); i += 2) {
                int moveNumber = (i / 2) + 1;
                Move whiteMove = moveHistory.get(i);
                String whiteSan = currentGame.moveToSan(whiteMove);

                String blackSan = null;
                if (i + 1 < moveHistory.size()) {
                    Move blackMove = moveHistory.get(i + 1);
                    blackSan = currentGame.moveToSan(blackMove);
                }

                pgnMoves.add(new PgnMoveRecord(moveNumber, whiteSan, blackSan));
            }

            // Write to file
            PgnWriter writer = new PgnWriter();
            File file = new File(filename);
            writer.write(file, metadata, pgnMoves);

            ui.displayMessage("Game saved to " + filename);

        } catch (IOException e) {
            ui.displayError("Failed to save game: " + e.getMessage());
        } catch (Exception e) {
            ui.displayError("Error saving game: " + e.getMessage());
        }
    }

    /**
     * Updates game state (check, checkmate, stalemate).
     */
    private static void updateGameState() {
        if (currentGame == null) {
            return;
        }

        // Do not overwrite terminal/non-position states set by commands (resign/draw) or previous resolution.
        GameState existing = currentGame.getGameState();
        if (existing == GameState.RESIGNATION ||
            existing == GameState.DRAW_BY_AGREEMENT ||
            existing == GameState.TIME_OUT ||
            existing == GameState.CHECKMATE ||
            existing == GameState.STALEMATE) {
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
