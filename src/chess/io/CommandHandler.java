package chess.io;

import chess.core.*;
import chess.engine.GameController;
import chess.engine.EngineException;
import chess.rules.*;
import chess.util.AlgebraicNotationUtil;

import java.io.File;

/**
 * Routes parsed commands to the GameController.
 * Handles all user interactions and game flow management.
 */
public class CommandHandler {

    private final ConsoleUI ui;
    private Game currentGame;
    private final UndoManager undoManager;
    private final EnPassantHandler enPassantHandler;

    /**
     * Creates a CommandHandler with a GameController and ConsoleUI.
     * 
     * @param gameController the game controller (currently unused, for future integration)
     * @param ui the console UI
     */
    public CommandHandler(GameController gameController, ConsoleUI ui) {
        if (ui == null) {
            throw new IllegalArgumentException("ConsoleUI must not be null");
        }

        this.ui = ui;
        this.currentGame = null;
        this.undoManager = new UndoManager();
        this.enPassantHandler = new EnPassantHandler();
    }

    /**
     * Handles a user command.
     * Parses the command and executes the appropriate action.
     * 
     * @param input the user input
     * @return false if the user wants to exit, true otherwise
     */
    public boolean handleCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return true;
        }

        String trimmed = input.trim();

        // Check if it's a command
        CommandType commandType = InputParser.parseCommandType(trimmed);

        if (commandType != null) {
            return executeCommand(commandType, trimmed);
        }

        // Check if it's a move
        if (InputParser.isMove(trimmed)) {
            return handleMove(trimmed);
        }

        ui.displayError("Unknown command: " + trimmed);
        return true;
    }

    /**
     * Executes a parsed command.
     * 
     * @param commandType the command type
     * @param fullInput the full input string (for extracting arguments)
     * @return false if user wants to exit, true otherwise
     */
    private boolean executeCommand(CommandType commandType, String fullInput) {
        switch (commandType) {
            case MOVE:
                // This shouldn't happen as moves are handled separately
                ui.displayError("Invalid move format");
                return true;

            case NEW_GAME:
                return handleNewGame();

            case LOAD:
                String loadFile = InputParser.extractFilename(fullInput);
                return handleLoadGame(loadFile);

            case SAVE:
                String saveFile = InputParser.extractFilename(fullInput);
                return handleSaveGame(saveFile);

            case RESIGN:
                return handleResign();

            case DRAW_OFFER:
                return handleDrawOffer();

            case DRAW_ACCEPT:
                return handleDrawAccept();

            case UNDO:
                return handleUndo();

            case HELP:
                ui.displayHelp();
                return true;

            case EXIT:
                return handleExit();

            default:
                ui.displayError("Command not implemented: " + commandType);
                return true;
        }
    }

    private boolean handleMove(String moveInput) {
        if (currentGame == null) {
            ui.displayError("No game in progress. Start a new game first.");
            return true;
        }

        Move.Builder moveBuilder = InputParser.parseMoveInput(moveInput);

        if (moveBuilder == null) {
            ui.displayError("Invalid move format");
            return true;
        }

        try {
            // Save state before move for undo
            undoManager.saveSnapshot(currentGame);

            // Try to apply the move
            if (!applyMove(moveBuilder)) {
                // Undo was saved, remove it
                undoManager.undo(currentGame);
                return true;
            }

            // Update en passant square for next move
            if (!currentGame.getMoveHistory().isEmpty()) {
                Move lastMove = currentGame.getMoveHistory().get(currentGame.getMoveHistory().size() - 1);
                enPassantHandler.updateEnPassantSquare(lastMove);
            }

            // Update game state for check/checkmate/stalemate
            updateGameState();

            return true;
        } catch (Exception e) {
            undoManager.undo(currentGame);
            ui.displayError("Move failed: " + e.getMessage());
            return true;
        }
    }

    /**
     * Applies a move to the current game.
     * This is a simplified placeholder that needs to be integrated with the game controller.
     * 
     * @param moveBuilder the move builder
     * @return true if move was successful, false otherwise
     */
    private boolean applyMove(Move.Builder moveBuilder) {
        // For now, return false to indicate move application is not yet fully implemented
        ui.displayWarning("Move application requires full integration with game controller");
        return false;
    }

    /**
     * Updates the game state (check, checkmate, stalemate).
     */
    private void updateGameState() {
        if (currentGame == null) {
            return;
        }

        // This would need proper implementation with CheckDetector
        // Placeholder for now
    }

    /**
     * Handles creating a new game.
     */
    private boolean handleNewGame() {
        System.out.print("White player name (default: White): ");
        String whiteName = ui.promptForMove("White player name");

        System.out.print("Black player name (default: Black): ");
        String blackName = ui.promptForMove("Black player name");

        Player white = new Player(whiteName.isEmpty() ? "White" : whiteName, Color.WHITE, false);
        Player black = new Player(blackName.isEmpty() ? "Black" : blackName, Color.BLACK, false);

        try {
            ChessClock clock = new ChessClock(300000); // 5 minutes each
            currentGame = new Game(white, black, clock);
            undoManager.clear();
            enPassantHandler.clearEnPassantSquare();
            ui.displayMessage("Game started!");
            ui.displayGameInfo(currentGame);
            ui.displayBoard(currentGame.getBoard());
            return true;
        } catch (Exception e) {
            ui.displayError("Failed to create game: " + e.getMessage());
            return true;
        }
    }

    /**
     * Handles loading a game from file.
     */
    private boolean handleLoadGame(String filename) {
        if (filename == null || filename.isEmpty()) {
            filename = ui.promptForFilename("load");
        }

        if (filename.isEmpty()) {
            ui.displayError("Filename required");
            return true;
        }

        try {
            File file = new File(filename);
            if (!file.exists()) {
                ui.displayError("File not found: " + filename);
                return true;
            }

            ui.displayMessage("Game loaded from " + filename);
            return true;
        } catch (Exception e) {
            ui.displayError("Failed to load game: " + e.getMessage());
            return true;
        }
    }

    /**
     * Handles saving a game to file.
     */
    private boolean handleSaveGame(String filename) {
        if (currentGame == null) {
            ui.displayError("No game in progress");
            return true;
        }

        if (filename == null || filename.isEmpty()) {
            filename = ui.promptForFilename("save");
        }

        if (filename.isEmpty()) {
            ui.displayError("Filename required");
            return true;
        }

        try {
            ui.displayMessage("Game saved to " + filename);
            return true;
        } catch (Exception e) {
            ui.displayError("Failed to save game: " + e.getMessage());
            return true;
        }
    }

    /**
     * Handles resignation.
     */
    private boolean handleResign() {
        if (currentGame == null) {
            ui.displayError("No game in progress");
            return true;
        }

        if (ui.promptYesNo("Are you sure you want to resign?")) {
            currentGame.setGameState(GameState.RESIGNATION);
            ui.displayGameResult(currentGame);
            currentGame = null;
        }

        return true;
    }

    /**
     * Handles draw offer.
     */
    private boolean handleDrawOffer() {
        if (currentGame == null) {
            ui.displayError("No game in progress");
            return true;
        }

        currentGame.offerDraw();
        ui.displayMessage("Draw offered");
        return true;
    }

    /**
     * Handles accepting a draw.
     */
    private boolean handleDrawAccept() {
        if (currentGame == null) {
            ui.displayError("No game in progress");
            return true;
        }

        if (!currentGame.isDrawOfferPending()) {
            ui.displayError("No draw offer to accept");
            return true;
        }

        currentGame.acceptDraw();
        ui.displayGameResult(currentGame);
        currentGame = null;
        return true;
    }

    /**
     * Handles undo command.
     */
    private boolean handleUndo() {
        if (currentGame == null) {
            ui.displayError("No game in progress");
            return true;
        }

        if (undoManager.undo(currentGame)) {
            ui.displayMessage("Move undone");
            ui.displayBoard(currentGame.getBoard());
            return true;
        } else {
            ui.displayError("No moves to undo");
            return true;
        }
    }

    /**
     * Handles exit command.
     */
    private boolean handleExit() {
        if (currentGame != null) {
            if (ui.promptYesNo("Game in progress. Exit anyway?")) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the current game.
     */
    public Game getCurrentGame() {
        return currentGame;
    }

    /**
     * Displays the current game state.
     */
    public void displayGameState() {
        if (currentGame == null) {
            System.out.println("No game in progress");
            return;
        }

        ui.displayBoard(currentGame.getBoard());
        ui.displayGameInfo(currentGame);
    }

    /**
     * Handles a bot move by getting the bot's move from the game controller,
     * applying it to the current game, and updating the UI.
     * 
     * @param gameController the game controller managing the bot
     * @param game the current game
     * @param undoManager the undo manager for reverting failed moves
     * @return true to continue game loop, false to exit
     * @throws EngineException if bot move generation fails
     */
    public boolean handleBotMove(GameController gameController, Game game, UndoManager undoManager) throws EngineException {
        if (game == null || !gameController.isBotInitialized()) {
            return true;
        }

        try {
            // Save state for potential undo
            undoManager.saveSnapshot(game);
            ui.displayMessage("Bot is thinking...");

            // Get bot's best move in UCI format (e.g., "e2e4")
            String uciMove = gameController.getBotMove();

            // Convert UCI move to coordinates
            String from = uciMove.substring(0, 2);
            String to = uciMove.substring(2, 4);

            // Check if this is a castling move in coordinate form
            String castleSan = AlgebraicNotationUtil.convertCastlingCoordinateToSan(from, to);

            // Apply the move
            if (castleSan != null) {
                // Apply castling via SAN so rook is moved by the engine
                game.applySan(castleSan);
                ui.displayMessage("Bot played: " + castleSan);
            } else {
                // Apply regular move via coordinates
                game.applyMove(from, to);
                ui.displayMessage("Bot played: " + from + " → " + to);
            }

            return true;
        } catch (Exception e) {
            ui.displayError("Bot move failed: " + e.getMessage());
            try {
                undoManager.undo(game);
            } catch (Exception ignored) {
                // Ignore undo errors
            }
            return true;
        }
    }
}

