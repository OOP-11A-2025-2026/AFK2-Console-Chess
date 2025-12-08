package chess.engine;

import chess.core.*;
import chess.pgn.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameController
{

    private final PgnWriter pgnWriter;
    private final PgnParser pgnParser;

    private Game game;
    private final List<PgnMoveRecord> pgnMoves;
    private PgnGameMetadata metadata;

    public GameController()
    {
        this.pgnWriter = new PgnWriter();
        this.pgnParser = new PgnParser();
        this.pgnMoves = new ArrayList<>();
        this.metadata = new PgnGameMetadata();
    }

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
            if (startingFen == null || startingFen.trim().isEmpty()) {this.game = new Game(white, black);}
            else {this.game = new Game(white, black, startingFen);}
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
                Object posFrom = fromFactory.invoke(null, fromSquare);
                Object posTo = fromFactory.invoke(null, toSquare);

                try
                {
                    java.lang.reflect.Method make = game.getClass().getMethod("applyMove", Class.forName("chess.core.Move"));
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
            // Fallback names if missing
            if (whiteName == null) whiteName = "White";
            if (blackName == null) blackName = "Black";

            Player white = new Player(whiteName, chess.core.Color.WHITE, false);
            Player black = new Player(blackName, chess.core.Color.BLACK, false);

            newGame(white, black, null);

            this.metadata = meta;
            this.pgnMoves.clear();

            for (PgnMoveRecord rec : moves)
            {
                if (rec.getWhiteSan() != null) {applyAlgebraicMove(rec.getWhiteSan());}
                if (rec.getBlackSan() != null) {applyAlgebraicMove(rec.getBlackSan());}
            }

        }
        catch (IOException | PgnFormatException e)
        {
            throw new EngineException("Failed to parse PGN file: " + e.getMessage(), e);
        }
    }

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

    private String determineResignResultTag(Player resigningPlayer)
    {
        if (resigningPlayer.getColor() == chess.core.Color.WHITE) {return "0-1";}
        else {return "1-0";}
    }

    public synchronized void offerDraw(Player byPlayer) throws EngineException
    {
        ensureGameLoaded();
        if (byPlayer == null) throw new EngineException("player must not be null");
        metadata.setTag("DrawOfferBy", byPlayer.getName());
    }

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

    public synchronized List<PgnMoveRecord> getPgnMoves() {return new ArrayList<>(pgnMoves);}

    public synchronized PgnGameMetadata getMetadata() {return metadata;}

    public synchronized Game getGame() {return game;}

    private void ensureGameLoaded() throws EngineException {if (this.game == null) throw new EngineException("No active game; call newGame(...) first");}

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
}
