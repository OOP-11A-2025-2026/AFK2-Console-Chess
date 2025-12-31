package chess.engine;

import chess.core.*;
import chess.pgn.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages the chess game state, move application, and PGN (Portable Game Notation) handling.
 * Coordinates between the game logic, UI, and external engines like Stockfish.
 */
public class GameController
{

    private final PgnWriter pgnWriter;
    private final PgnParser pgnParser;

    private Game game;
    private final List<PgnMoveRecord> pgnMoves;
    private PgnGameMetadata metadata;
    
    private ChessEngine engine;
    private BotDifficulty botDifficulty;

    /**
     * Creates a GameController with default PGN writer and parser.
     */
    public GameController()
    {
        this.pgnWriter = new PgnWriter();
        this.pgnParser = new PgnParser();
        this.pgnMoves = new ArrayList<>();
        this.metadata = new PgnGameMetadata();
    }

    /**
     * Creates a new chess game with specified players and optional starting FEN.
     * Initializes game metadata and PGN tracking.
     * 
     * @param white the white player
     * @param black the black player
     * @param startingFen the starting position in FEN notation (null for standard starting position)
     * @throws EngineException if players are invalid or game creation fails
     */
    public synchronized void newGame(Player white, Player black, String startingFen) throws EngineException
    {
        Objects.requireNonNull(white, "white player must not be null");
        Objects.requireNonNull(black, "black player must not be null");
        if (white.getColor() == black.getColor()) {throw new EngineException("Players must have opposite colors");}
        metadata = new PgnGameMetadata();
        metadata.setWhite(nonEmptyOrDefault(white.getName(), "White"));
        metadata.setBlack(nonEmptyOrDefault(black.getName(), "Black"));
        metadata.setEvent("Casual Game");
        metadata.setSite("-");
        metadata.setDate(java.time.LocalDate.now().toString());
        metadata.setRound("-");

        pgnMoves.clear();

        try
        {
            chess.core.ChessClock clock = new chess.core.ChessClock(5 * 60 * 1000); // 5 minutes
            if (startingFen == null || startingFen.trim().isEmpty()) {
                this.game = new Game(white, black, clock);
            } else {
                this.game = new Game(white, black, startingFen);
            }
        }
        catch (Exception ex)
        {
            throw new EngineException("Failed to create new Game instance: " + ex.getMessage(), ex);
        }
    }

    private String nonEmptyOrDefault(String s, String def)
    {
        if (s == null || s.trim().isEmpty()) return def;
        return s.trim();
    }

    /**
     * Applies an algebraic notation move to the current game.
     * Converts SAN (Standard Algebraic Notation) to a legal move and applies it.
     * Automatically updates PGN move record and game metadata.
     * Requires an active game to be loaded.
     * 
     * @param san the move in Standard Algebraic Notation (e.g., "e4", "Nf3", "O-O")
     * @throws EngineException if no game is loaded, move is invalid, or application fails
     */
    public synchronized void applyAlgebraicMove(String san) throws EngineException
    {
        ensureGameLoaded();
        if (san == null || san.trim().isEmpty()) throw new EngineException("SAN move must not be empty");

        san = san.trim();
        try
        {
            boolean applied = tryApplySanOnGame(san);
            if (!applied)
            {
                try
                {
                    java.lang.reflect.Method resolve = game.getClass().getMethod("resolveSan", String.class);
                    java.lang.Object maybeMove = resolve.invoke(game, san);
                    if (maybeMove == null) {throw new EngineException("Could not resolve SAN to a legal move: " + san);}
                    java.lang.reflect.Method apply = game.getClass().getMethod("applyMove", chess.core.Move.class);
                    apply.invoke(game, maybeMove);
                }
                catch (NoSuchMethodException nsme)
                {
                    throw new EngineException("Game API does not support SAN application; implement Game.applySan or Game.resolveSan+applyMove", nsme);
                }
            }

            boolean whiteToMove = isWhiteToMoveAccordingToGame();
            if (whiteToMove)
            {
                int moveNumber = pgnMoves.size() + 1;
                pgnMoves.add(new PgnMoveRecord(moveNumber, san, null));
            }
            else
            {
                if (pgnMoves.isEmpty())
                {
                    int moveNumber = 1;
                    pgnMoves.add(new PgnMoveRecord(moveNumber, null, san));
                }
                else
                {
                    PgnMoveRecord last = pgnMoves.get(pgnMoves.size() - 1);
                    PgnMoveRecord updated = new PgnMoveRecord(last.getMoveNumber(), last.getWhiteSan(), san);
                    pgnMoves.set(pgnMoves.size() - 1, updated);
                }
            }
        }
        catch (EngineException ee)
        {
            throw ee;
        }
        catch (Exception ex)
        {
            throw new EngineException("Failed to apply SAN move: " + ex.getMessage(), ex);
        }
        updateResultTagFromGameState();
    }

    private boolean tryApplySanOnGame(String san) throws Exception
    {
        try
        {
            java.lang.reflect.Method m = game.getClass().getMethod("applySan", String.class);
            m.invoke(game, san);
            return true;
        }
        catch (NoSuchMethodException e)
        {
            return false;
        }
    }

    /**
     * Applies a coordinate-based move to the current game.
     * Converts from/to square coordinates (e.g., "e2" to "e4") to a legal move.
     * Automatically updates PGN move record and game metadata.
     * Requires an active game to be loaded.
     * 
     * @param fromSquare the source square in algebraic notation (e.g., "e2")
     * @param toSquare the destination square in algebraic notation (e.g., "e4")
     * @throws EngineException if no game is loaded, move is invalid, or application fails
     */
    public synchronized void applyCoordinateMove(String fromSquare, String toSquare) throws EngineException
    {
        ensureGameLoaded();
        if (fromSquare == null || toSquare == null) throw new EngineException("from/to squares must not be null");
        fromSquare = fromSquare.trim();
        toSquare = toSquare.trim();
        if (fromSquare.isEmpty() || toSquare.isEmpty()) throw new EngineException("from/to squares must not be empty");

        try
        {
            try
            {
                java.lang.reflect.Method m = game.getClass().getMethod("applyMove", String.class, String.class);
                m.invoke(game, fromSquare, toSquare);
            }
            catch (NoSuchMethodException nsme)
            {
                java.lang.Class<?> posClass = Class.forName("chess.core.Position");
                java.lang.reflect.Method fromFactory = posClass.getMethod("fromAlgebraic", String.class);
                fromFactory.invoke(null, fromSquare);
                fromFactory.invoke(null, toSquare);

                try
                {
                    game.getClass().getMethod("applyMove", Class.forName("chess.core.Move"));
                    throw new EngineException("Coordinate-apply fallback requires Game.resolveMove(Position, Position) or Game.applyMove(from,to) support");
                }
                catch (NoSuchMethodException e)
                {
                    throw new EngineException("Game does not expose an applyMove(from,to) method; implement one or adapt GameController", e);
                }
            }

            String san = tryGetLastMoveSanFromGame();
            boolean whiteToMove = isWhiteToMoveAccordingToGame();
            if (whiteToMove)
            {
                int moveNumber = pgnMoves.size() + 1;
                pgnMoves.add(new PgnMoveRecord(moveNumber, san != null ? san : fromSquare + "-" + toSquare, null));
            }
            else
            {
                if (pgnMoves.isEmpty())
                {
                    pgnMoves.add(new PgnMoveRecord(1, null, san != null ? san : fromSquare + "-" + toSquare));
                }
                else
                {
                    PgnMoveRecord last = pgnMoves.get(pgnMoves.size() - 1);
                    PgnMoveRecord updated = new PgnMoveRecord(last.getMoveNumber(), last.getWhiteSan(), san != null ? san : fromSquare + "-" + toSquare);
                    pgnMoves.set(pgnMoves.size() - 1, updated);
                }
            }

        }
        catch (EngineException ee)
        {
            throw ee;
        }
        catch (Exception ex)
        {
            throw new EngineException("Failed to apply coordinate move: " + ex.getMessage(), ex);
        }

        updateResultTagFromGameState();
    }

    /**
     * Saves the current game to a PGN file.
     * Includes all metadata tags and moves in standard PGN format.
     * Updates the Result tag from current game state before saving.
     * Requires an active game to be loaded.
     * 
     * @param file the output PGN file to write to
     * @throws EngineException if no game is loaded or file I/O fails
     */
    public synchronized void saveGame(File file) throws EngineException
    {
        ensureGameLoaded();
        Objects.requireNonNull(file, "file must not be null");
        try
        {
            updateResultTagFromGameState();
            pgnWriter.write(file, metadata, new ArrayList<>(pgnMoves));
        }
        catch (IOException ioe)
        {
            throw new EngineException("Failed to write PGN file: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Loads a game from a PGN file.
     * Parses metadata and all moves, then reconstructs the game by applying moves sequentially.
     * Creates a new game with the players specified in the PGN metadata.
     * 
     * @param file the PGN file to load from
     * @throws EngineException if file reading, parsing, or move application fails
     */
    public synchronized void loadGame(File file) throws EngineException
    {
        Objects.requireNonNull(file, "file must not be null");
        try
        {
            PgnParser.ParseResult res = pgnParser.parse(file);
            PgnGameMetadata meta = res.getMetadata();
            List<PgnMoveRecord> moves = res.getMoves();

            String whiteName = meta.getTag("White");
            String blackName = meta.getTag("Black");
            if (whiteName == null) whiteName = "White";
            if (blackName == null) blackName = "Black";

            Player white = new Player(whiteName, chess.core.Color.WHITE, false);
            Player black = new Player(blackName, chess.core.Color.BLACK, false);

            newGame(white, black, null);

            this.metadata = meta;
            this.pgnMoves.clear();

            for (PgnMoveRecord rec : moves)
            {
                if (rec.getWhiteSan() != null) {
                    try {
                        applyAlgebraicMove(rec.getWhiteSan());
                    } catch (Exception e) {
                        throw new EngineException("Failed to apply white move " + rec.getMoveNumber() + ": " + rec.getWhiteSan() + " - " + e.getMessage(), e);
                    }
                }
                if (rec.getBlackSan() != null) {
                    try {
                        applyAlgebraicMove(rec.getBlackSan());
                    } catch (Exception e) {
                        throw new EngineException("Failed to apply black move " + rec.getMoveNumber() + ": " + rec.getBlackSan() + " - " + e.getMessage(), e);
                    }
                }
            }

        }
        catch (IOException | PgnFormatException e)
        {
            throw new EngineException("Failed to parse PGN file: " + e.getMessage(), e);
        }
    }

    /**
     * Records resignation of a player in the game.
     * Updates game state to RESIGNATION and sets the Result tag accordingly.
     * Requires an active game to be loaded.
     * 
     * @param p the player who is resigning
     * @throws EngineException if no game is loaded or resignation fails
     */
    public synchronized void resign(Player p) throws EngineException
    {
        ensureGameLoaded();
        if (p == null) throw new EngineException("player must not be null");
        try
        {
            java.lang.reflect.Method resignMethod = game.getClass().getMethod("resign", chess.core.Player.class);
            resignMethod.invoke(game, p);
        }
        catch (NoSuchMethodException ex)
        {
            try
            {
                java.lang.reflect.Method setState = game.getClass().getMethod("setState", chess.core.GameState.class);
                setState.invoke(game, chess.core.GameState.RESIGNATION);
            }
            catch (Exception e) { }
        }
        catch (Exception ex)
        {
            throw new EngineException("Failed to register resignation: " + ex.getMessage(), ex);
        }

        String resultTag = determineResignResultTag(p);
        metadata.setResult(resultTag);
    }

    /**
     * Determines the result tag value for a player's resignation.
     * Returns "1-0" if white resigns (black wins), "0-1" if black resigns (white wins).
     * 
     * @param resigningPlayer the player who is resigning
     * @return the PGN Result tag value
     */
    private String determineResignResultTag(Player resigningPlayer)
    {
        if (resigningPlayer.getColor() == chess.core.Color.WHITE) {return "0-1";}
        else {return "1-0";}
    }

    /**
     * Records a draw offer from a player.
     * Stores the player's name in the DrawOfferBy metadata tag.
     * Requires an active game to be loaded.
     * 
     * @param byPlayer the player offering the draw
     * @throws EngineException if no game is loaded
     */
    public synchronized void offerDraw(Player byPlayer) throws EngineException
    {
        ensureGameLoaded();
        if (byPlayer == null) throw new EngineException("player must not be null");
        metadata.setTag("DrawOfferBy", byPlayer.getName());
    }

    /**
     * Accepts a pending draw offer.
     * Sets the Result tag to "1/2-1/2" (draw) and updates game state.
     * Requires an active game to be loaded.
     * 
     * @throws EngineException if no game is loaded
     */
    public synchronized void acceptDraw() throws EngineException
    {
        ensureGameLoaded();
        metadata.setResult("1/2-1/2");
        try
        {
            java.lang.reflect.Method setState = game.getClass().getMethod("setState", chess.core.GameState.class);
            setState.invoke(game, chess.core.GameState.DRAW_BY_AGREEMENT);
        }
        catch (Exception ignored) { }
    }

    /**
     * Undoes the last move in the game.
     * Calls the game's undo method and removes the last move from PGN records.
     * Requires an active game to be loaded.
     * 
     * @throws EngineException if no game is loaded or undo fails
     */
    public synchronized void undo() throws EngineException
    {
        ensureGameLoaded();
        try
        {
            java.lang.reflect.Method undoMethod = game.getClass().getMethod("undo");
            undoMethod.invoke(game);
        }
        catch (NoSuchMethodException nsme)
        {
            throw new EngineException("Game does not provide undo(); please implement Game.undo() or provide UndoManager integration", nsme);
        }
        catch (Exception ex)
        {
            throw new EngineException("Failed to undo last move: " + ex.getMessage(), ex);
        }

        if (!pgnMoves.isEmpty())
        {
            PgnMoveRecord last = pgnMoves.get(pgnMoves.size() - 1);
            if (last.getBlackSan() != null)
            {
                PgnMoveRecord updated = new PgnMoveRecord(last.getMoveNumber(), last.getWhiteSan(), null);
                pgnMoves.set(pgnMoves.size() - 1, updated);
            }
            else
            {
                pgnMoves.remove(pgnMoves.size() - 1);
            }
        }
    }

    /**
     * Gets a copy of the PGN move records.
     * Returns an unmodifiable list of all moves in PGN format.
     * 
     * @return a list of PgnMoveRecord objects
     */
    public synchronized List<PgnMoveRecord> getPgnMoves() {return new ArrayList<>(pgnMoves);}

    /**
     * Gets the current game metadata.
     * Returns metadata tags such as Event, Site, Date, Round, White, Black, and Result.
     * 
     * @return the PgnGameMetadata instance
     */
    public synchronized PgnGameMetadata getMetadata() {return metadata;}

    /**
     * Gets the currently active game.
     * Returns null if no game is loaded.
     * 
     * @return the current Game instance, or null
     */
    public synchronized Game getGame() {return game;}

    /**
     * Ensures a game is loaded before operations.
     * Throws EngineException if no active game exists.
     * 
     * @throws EngineException if no game is currently loaded
     */
    private void ensureGameLoaded() throws EngineException {if (this.game == null) throw new EngineException("No active game; call newGame(...) first");}

    /**
     * Attempts to get the last move in SAN notation from the game.
     * Uses reflection to call Game.getLastMoveSan() if available.
     * 
     * @return the last move in SAN format, or null if method unavailable or no moves
     */
    private String tryGetLastMoveSanFromGame()
    {
        try
        {
            java.lang.reflect.Method m = game.getClass().getMethod("getLastMoveSan");
            Object o = m.invoke(game);
            if (o instanceof String) return (String) o;
        }
        catch (Exception ignored) { }
        return null;
    }

    /**
     * Checks if it's white's turn to move according to the game.
     * Uses reflection to call Game.isWhiteToMove() if available,
     * otherwise checks the current player's color.
     * Falls back to counting moves in the PGN record.
     * 
     * @return true if white is to move, false if black is to move
     */
    private boolean isWhiteToMoveAccordingToGame()
    {
        try
        {
            java.lang.reflect.Method m = game.getClass().getMethod("isWhiteToMove");
            Object o = m.invoke(game);
            if (o instanceof Boolean) return (Boolean) o;
        }
        catch (Exception ignored) { }

        try
        {
            java.lang.reflect.Method m2 = game.getClass().getMethod("getCurrentPlayer");
            Object p = null;
            p = m2.invoke(game);
            if (p instanceof Player) {return ((Player) p).getColor() == chess.core.Color.WHITE;}
        }
        catch (Exception ignored) { }

        int ply = 0;
        for (PgnMoveRecord rec : pgnMoves)
        {
            if (rec.getWhiteSan() != null) ply++;
            if (rec.getBlackSan() != null) ply++;
        }
        return (ply % 2) == 0;
    }

    /**
     * Updates the PGN Result tag based on the current game state.
     * Analyzes checkmate, stalemate, resignation, and draw conditions,
     * and sets the appropriate PGN result code.
     */
    private void updateResultTagFromGameState()
    {
        try
        {
            java.lang.reflect.Method stateMethod = game.getClass().getMethod("getState");
            Object s = stateMethod.invoke(game);
            if (s instanceof chess.core.GameState)
            {
                chess.core.GameState gs = (chess.core.GameState) s;
                switch (gs)
                {
                    case CHECKMATE:
                        try
                        {
                            java.lang.reflect.Method winner = game.getClass().getMethod("getWinner");
                            Object w = winner.invoke(game);
                            if (w instanceof Player)
                            {
                                Player p = (Player) w;
                                metadata.setResult(p.getColor() == chess.core.Color.WHITE ? "1-0" : "0-1");
                            }
                        }
                        catch (Exception ignored) { }
                        break;
                    case STALEMATE:
                    case DRAW_BY_AGREEMENT:
                        metadata.setResult("1/2-1/2");
                        break;
                    case RESIGNATION:
                        try
                        {
                            java.lang.reflect.Method winner = game.getClass().getMethod("getWinner");
                            Object w = winner.invoke(game);
                            if (w instanceof Player)
                            {
                                Player p = (Player) w;
                                metadata.setResult(p.getColor() == chess.core.Color.WHITE ? "1-0" : "0-1");
                            }
                        }
                        catch (Exception ignored) { }
                        break;
                    default:
                        break;
                }
            }
        }
        catch (Exception ignored) { }
    }

    /**
     * Initializes the bot engine with the given difficulty level.
     * Starts the Stockfish process and sets the appropriate skill level.
     * 
     * @param difficulty the BotDifficulty level (BEGINNER to GRANDMASTER)
     * @throws EngineException if engine startup fails
     */
    public synchronized void initializeBot(BotDifficulty difficulty) throws EngineException {
        try {
            this.botDifficulty = difficulty;
            this.engine = new StockfishEngine();
            this.engine.start();
            this.engine.setSkillLevel(difficulty.getSkillLevel());
        } catch (IOException e) {
            throw new EngineException("Failed to initialize bot: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the best move from the bot for the current game position.
     * Calculates appropriate search depth based on difficulty and queries the engine.
     * 
     * @return the best move in UCI format (e.g., "e2e4")
     * @throws EngineException if bot is not initialized or move calculation fails
     */
    public synchronized String getBotMove() throws EngineException {
        if (engine == null) {
            throw new EngineException("Bot not initialized");
        }
        if (game == null) {
            throw new EngineException("No active game");
        }

        try {
            int depth = calculateDepthFromDifficulty(botDifficulty);
            Color sideToMove = game.getCurrentPlayerColor();
            String move = engine.bestMove(game.getBoard(), sideToMove, depth);
            return move;
        } catch (IOException e) {
            throw new EngineException("Bot failed to calculate move: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates appropriate search depth for the engine based on difficulty level.
     * Higher difficulties use deeper searches for stronger play.
     * 
     * @param difficulty the BotDifficulty level
     * @return the recommended search depth (typically 5-35)
     */
    private int calculateDepthFromDifficulty(BotDifficulty difficulty) {
        switch (difficulty) {
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
                return 10;
        }
    }

    /**
     * Shuts down the bot engine and releases resources.
     * Should be called when the bot is no longer needed.
     * 
     * @throws EngineException if engine shutdown fails
     */
    public synchronized void shutdownBot() throws EngineException {
        if (engine != null) {
            try {
                engine.stop();
                engine = null;
            } catch (IOException e) {
                throw new EngineException("Failed to shutdown bot: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Checks if the bot engine is currently initialized and running.
     * 
     * @return true if bot is ready to play, false otherwise
     */
    public synchronized boolean isBotInitialized() {
        return engine != null;
    }

    /**
     * Sets up a new 2-player game with given player names.
     * Creates players with appropriate colors and initializes the game.
     * 
     * @param whiteName name for white player
     * @param blackName name for black player
     * @throws EngineException if game creation fails
     */
    public synchronized void setupNewGame(String whiteName, String blackName) throws EngineException {
        if (whiteName == null || whiteName.trim().isEmpty()) {
            whiteName = "Player 1";
        }
        if (blackName == null || blackName.trim().isEmpty()) {
            blackName = "Player 2";
        }

        whiteName = whiteName.trim();
        blackName = blackName.trim();

        try {
            Player white = new Player(whiteName, Color.WHITE, false);
            Player black = new Player(blackName, Color.BLACK, false);
            newGame(white, black, null);
        } catch (EngineException e) {
            throw e;
        } catch (Exception e) {
            throw new EngineException("Failed to create new game: " + e.getMessage(), e);
        }
    }

    /**
     * Sets up a new game against the bot with given difficulty.
     * Creates a human player and a bot player with appropriate colors.
     * Initializes the bot engine with the specified difficulty.
     * 
     * @param playerName name for the human player
     * @param difficulty bot difficulty level
     * @param playerColor color for the human player (WHITE or BLACK)
     * @return true if bot plays as white (moves first), false if human plays as white
     * @throws EngineException if game creation or bot initialization fails
     */
    public synchronized boolean setupBotGame(String playerName, BotDifficulty difficulty, Color playerColor) throws EngineException {
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Human";
        }
        if (difficulty == null) {
            difficulty = BotDifficulty.INTERMEDIATE;
        }
        if (playerColor == null) {
            playerColor = Color.WHITE;
        }

        playerName = playerName.trim();

        try {
            Player human = new Player(playerName, playerColor, false);
            Player bot = new Player("Stockfish " + difficulty.name(), playerColor.opposite(), true);

            Player white = playerColor == Color.WHITE ? human : bot;
            Player black = playerColor == Color.BLACK ? human : bot;

            // Set up the game first
            newGame(white, black, null);

            // Then initialize the bot
            initializeBot(difficulty);

            // Return true if bot is playing white (i.e., should move first)
            return bot.getColor() == Color.WHITE;
        } catch (EngineException e) {
            throw e;
        } catch (Exception e) {
            throw new EngineException("Failed to create bot game: " + e.getMessage(), e);
        }
    }
}