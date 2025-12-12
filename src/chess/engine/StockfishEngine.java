package chess.engine;

import java.io.*;

public class StockfishEngine {

    private Process engine;
    private BufferedWriter writer;
    private BufferedReader reader;

    /**
     * Starts the Stockfish engine process.
     */
    public boolean startEngine(String pathToEngine) {
        try {
            engine = new ProcessBuilder(pathToEngine)
                    .redirectErrorStream(true)
                    .start();

            writer = new BufferedWriter(new OutputStreamWriter(engine.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(engine.getInputStream()));

            sendCommand("uci");
            waitReady();

            return true;

        } catch (Exception e) {
            System.out.println("Failed to start Stockfish: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sends a UCI command to the engine.
     */
    public void sendCommand(String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
    }

    /**
     * Waits for Stockfish to return "readyok".
     */
    private void waitReady() throws IOException {
        sendCommand("isready");
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("readyok")) return;
        }
    }

    /**
     * Reads all available engine output for a limited time.
     */
    public String getOutput(long waitTimeMs) throws IOException {
        StringBuilder buffer = new StringBuilder();
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < waitTimeMs) {
            if (reader.ready()) {
                String line = reader.readLine();
                if (line == null) break;
                buffer.append(line).append("\n");
            }
        }
        return buffer.toString();
    }

    /**
     * Gets the best move using a FEN string.
     */
    public String getBestMove(String fen, int depth) throws IOException {
        sendCommand("position fen " + fen);
        sendCommand("go depth " + depth);

        String output = getOutput(2000);

        for (String line : output.split("\n")) {
            if (line.startsWith("bestmove")) {
                return line.split(" ")[1];  // returns "e2e4"
            }
        }

        return "none";
    }

    /**
     * Properly kills the engine process.
     */
    public void stopEngine() throws IOException {
        if (engine != null) {
            sendCommand("quit");
            engine.destroy();
        }
    }
}