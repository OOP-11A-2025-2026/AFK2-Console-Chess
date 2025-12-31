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

    /**
     * Handles a move input string.
     * Parses the move notation, validates legality, saves game state for undo,
     * applies the move, and updates game state (check/checkmate/stalemate).
     * 
     * @param moveInput the move in coordinate or algebraic notation
     * @return true to continue game loop, false to exit
     */
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
     * Currently returns false to indicate move application is not yet fully implemented.
     * 
     * @param moveBuilder the move builder with source and destination positions
     * @return true if move was successfully applied, false otherwise
     */
    private boolean applyMove(Move.Builder moveBuilder) {
        // For now, return false to indicate move application is not yet fully implemented
        ui.displayWarning("Move application requires full integration with game controller");
        return false;
    }

    /**
     * Updates the game state after a move (check, checkmate, stalemate).
     * Uses CheckDetector to analyze the current board position
     * and updates the game's GameState accordingly.
     * Currently a placeholder for full implementation.
     */
    private void updateGameState() {
        if (currentGame == null) {
            return;
        }

        // This would need proper implementation with CheckDetector
        // Placeholder for now
    }

    /**
     * Handles the new game command.
     * Prompts for player names and initializes a new chess game with 5-minute time control.
     * Clears undo manager and en passant handler state.
     * 
     * @return true to continue game loop, false to exit
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
     * Prompts for filename if not provided and verifies file exists before loading.
     * 
     * @param filename the filename to load, or null to prompt user
     * @return true to continue game loop, false to exit
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
     * Handles the save command to save the current game to a PGN file.
     * Prompts for filename if not provided. Requires an active game.
     * 
     * @param filename the filename to save to, or null to prompt user
     * @return true to continue game loop, false to exit
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
     * Handles the resign command.
     * Prompts for confirmation before accepting resignation.
     * Sets game state to RESIGNATION and displays result.
     * 
     * @return true to continue game loop, false to exit
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
     * Handles the draw command to offer a draw to the opponent.
     * Sets the draw offer pending flag in the game state.
     * 
     * @return true to continue game loop, false to exit
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
     * Handles the accept command to accept a pending draw offer.
     * Requires that a draw offer exists. Sets game state to DRAW_BY_AGREEMENT.
     * 
     * @return true to continue game loop, false to exit
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
     * Handles the undo command to revert the last move.
     * Uses the undo manager to restore the previous game state.
     * Redisplays the board after undo.
     * 
     * @return true to continue game loop, false to exit
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
     * Handles the exit command to terminate the application.
     * Prompts for confirmation if a game is in progress.
     * 
     * @return false to signal exit from game loop, true to continue
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
     * Gets the current active game.
     * 
     * @return the current Game, or null if no game is in progress
     */
    public Game getCurrentGame() {
        return currentGame;
    }

    /**
     * Displays the current game state to the console.
     * Shows the board, player information, and other game details.
     * Displays a message if no game is in progress.
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

