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
     * Initializes the UCI protocol communication and waits for engine readiness.
     * Throws IOException if Stockfish executable cannot be found or started.
     * 
     * @throws IOException if the engine process fails to start
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
     * Checks environment variable STOCKFISH_PATH first, then searches common installation paths
     * for both macOS and Windows systems.
     * 
     * @return the path to Stockfish executable, or null if not found
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
     * Lower skill levels make the engine weaker for easier play.
     * Must be called after engine is started.
     * 
     * @param level the skill level (0-20 inclusive)
     * @throws IllegalArgumentException if level is not in range 0-20
     * @throws IOException if communication with engine fails
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
     * Gets the best move for a position using UCI protocol.
     * Sends position in FEN format and searches to specified depth.
     * Dynamically adjusts timeout based on search depth.
     * 
     * @param board the current board position
     * @param sideToMove the color to move (WHITE or BLACK)
     * @param depth the search depth (higher = stronger but slower)
     * @return the best move in UCI format (e.g., "e2e4", "a7a8q")
     * @throws IOException if engine communication fails or no move found
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
     * Commands are sent via standard input to the Stockfish process.
     * 
     * @param command the UCI command to send
     * @throws IOException if writing to engine fails
     */
    private void sendCommand(String command) throws IOException {
        if (writer == null) throw new IOException("Engine not initialized");
        writer.write(command + "\n");
        writer.flush();
    }

    /**
     * Waits for Stockfish to send "uciok" response.
     * Part of the UCI protocol initialization sequence.
     * Times out after 5 seconds.
     * 
     * @throws IOException if timeout occurs or engine communication fails
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
     * Waits for Stockfish to return "readyok" response.
     * Used to synchronize before sending move search commands.
     * Times out after 5 seconds.
     * 
     * @throws IOException if timeout occurs or engine communication fails
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
     * Buffers all output from the engine within the specified timeout period.
     * Useful for collecting engine analysis output.
     * 
     * @param waitTimeMs the maximum time in milliseconds to wait for output
     * @return all buffered output lines concatenated with newlines
     * @throws IOException if reading from engine fails
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
     * Sends the quit command and waits for process termination.
     * If process doesn't terminate gracefully, forces destruction.
     * 
     * @throws IOException if process termination fails
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

    /**
     * Closes the engine resource (implements AutoCloseable).
     * Calls stop() to clean up the engine process.
     * Can be used in try-with-resources statements.
     * 
     * @throws IOException if engine cleanup fails
     */
    @Override
    public void close() throws IOException {
        stop();
    }
}