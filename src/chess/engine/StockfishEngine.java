package chess.engine;

import chess.core.Board;
import chess.core.Color;

import java.io.*;

/**
 * Stockfish UCI engine implementation.
 * Communicates with Stockfish via UCI protocol.
 */
public class StockfishEngine implements ChessEngine {

    private Process engine;
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean isRunning;
    private int skillLevel = 10;  // Default skill level

    /**
     * Starts the Stockfish engine process.
     */
    @Override
    public void start() throws IOException {
        try {
            String pathToEngine = findStockfishPath();
            if (pathToEngine == null) {
                throw new IOException("Stockfish not found. Please install Stockfish or provide path via environment variable STOCKFISH_PATH");
            }

            engine = new ProcessBuilder(pathToEngine)
                    .redirectErrorStream(true)
                    .start();

            writer = new BufferedWriter(new OutputStreamWriter(engine.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(engine.getInputStream()));

            sendCommand("uci");
            waitForUciOk();
            isRunning = true;

        } catch (Exception e) {
            throw new IOException("Failed to start Stockfish: " + e.getMessage(), e);
        }
    }

    /**
     * Finds Stockfish executable in common locations.
     */
    private String findStockfishPath() {
        // Check environment variable first
        String envPath = System.getenv("STOCKFISH_PATH");
        if (envPath != null && new File(envPath).exists()) {
            return envPath;
        }

        // Check common macOS paths
        String[] paths = {
            "/usr/local/bin/stockfish",
            "/opt/homebrew/bin/stockfish",
            "stockfish",  // In PATH
            "C:\\Program Files\\Stockfish\\stockfish.exe",  // Windows
            "C:\\Program Files (x86)\\Stockfish\\stockfish.exe"
        };

        for (String path : paths) {
            try {
                Process p = new ProcessBuilder(path, "--version").start();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = br.readLine();
                br.close();
                if (line != null && line.contains("Stockfish")) {
                    return path;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    /**
     * Sets the skill level for Stockfish (0-20, where 20 is max strength).
     */
    @Override
    public void setSkillLevel(int level) throws IOException {
        if (level < 0 || level > 20) {
            throw new IllegalArgumentException("Skill level must be 0-20");
        }
        this.skillLevel = level;
        if (isRunning) {
            sendCommand("setoption name Skill Level value " + level);
        }
    }

    /**
     * Gets the best move for a position.
     */
    @Override
    public String bestMove(Board board, Color sideToMove, int depth) throws IOException {
        if (!isRunning) {
            throw new IOException("Engine not running");
        }

        String fen = FenUtil.generateFEN(board, sideToMove);
        
        // Send position and wait until engine is ready
        sendCommand("position fen " + fen);
        waitReady();

        // Use depth search; allow more time for deeper searches
        sendCommand("go depth " + depth);

        long waitTimeMs = Math.min(30000, Math.max(3000, depth * 500)); // dynamic timeout with caps
        String output = getOutput(waitTimeMs);

        for (String line : output.split("\n")) {
            if (line.trim().startsWith("bestmove")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    return parts[1];  // returns "e2e4"
                }
            }
        }

        // As a fallback, try one more read with a longer timeout
        output = getOutput(5000);
        for (String line : output.split("\n")) {
            if (line.trim().startsWith("bestmove")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    return parts[1];
                }
            }
        }

        throw new IOException("No bestmove found in engine output (timeout)");
    }

    /**
     * Sends a UCI command to the engine.
     */
    private void sendCommand(String command) throws IOException {
        if (writer == null) throw new IOException("Engine not initialized");
        writer.write(command + "\n");
        writer.flush();
    }

    /**
     * Waits for Stockfish to send "uciok".
     */
    private void waitForUciOk() throws IOException {
        String line;
        long timeout = System.currentTimeMillis() + 5000;
        while ((line = reader.readLine()) != null) {
            if (line.contains("uciok")) return;
            if (System.currentTimeMillis() > timeout) {
                throw new IOException("UCI initialization timeout");
            }
        }
    }

    /**
     * Waits for Stockfish to return "readyok".
     */
    private void waitReady() throws IOException {
        sendCommand("isready");
        String line;
        long timeout = System.currentTimeMillis() + 5000;
        while ((line = reader.readLine()) != null) {
            if (line.contains("readyok")) return;
            if (System.currentTimeMillis() > timeout) {
                throw new IOException("readyok timeout");
            }
        }
    }

    /**
     * Reads all available engine output for a limited time.
     */
    private String getOutput(long waitTimeMs) throws IOException {
        StringBuilder buffer = new StringBuilder();
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < waitTimeMs) {
            try {
                if (reader.ready()) {
                    String line = reader.readLine();
                    if (line == null) break;
                    buffer.append(line).append("\n");
                }
            } catch (Exception e) {
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        return buffer.toString();
    }

    /**
     * Stops the Stockfish engine.
     */
    @Override
    public void stop() throws IOException {
        if (engine != null) {
            try {
                sendCommand("quit");
                engine.waitFor();
            } catch (Exception e) {
                engine.destroy();
            }
            isRunning = false;
        }
    }
}