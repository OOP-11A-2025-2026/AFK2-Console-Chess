import chess.core.*;
import chess.engine.*;
import chess.util.BoardPrinter;

/**
 * Quick test to verify bot functionality
 */
public class TestBotGame {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing Stockfish Bot Integration ===\n");

        // Test 1: Initialize bot
        System.out.println("Test 1: Initializing Stockfish engine...");
        try (StockfishEngine engine = new StockfishEngine()) {
            try {
                engine.start();
                System.out.println("✓ Stockfish started successfully");
                engine.setSkillLevel(10);
                System.out.println("✓ Skill level set to 10");
            } catch (Exception e) {
                System.err.println("✗ Failed to start Stockfish: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            // Test 2: Create game and get bot move
            System.out.println("\nTest 2: Creating game and getting bot move...");
            try {
                Player white = new Player("Human", Color.WHITE, false);
                Player black = new Player("Stockfish", Color.BLACK, true);
                ChessClock clock = new ChessClock(5 * 60 * 1000);
                Game game = new Game(white, black, clock);

                System.out.println("Initial board:");
                BoardPrinter.printBoard(game.getBoard());

                // Get bot move
                String bestMove = engine.bestMove(game.getBoard(), Color.WHITE, 8);
                System.out.println("✓ Bot suggested move (UCI format): " + bestMove);

                // Apply the move
                String from = bestMove.substring(0, 2);
                String to = bestMove.substring(2, 4);
                game.applyMove(from, to);
                System.out.println("✓ Move applied: " + from + " → " + to);

                System.out.println("\nBoard after move:");
                BoardPrinter.printBoard(game.getBoard());

            } catch (Exception e) {
                System.err.println("✗ Test failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("\n=== All tests completed ===");
    }
}
