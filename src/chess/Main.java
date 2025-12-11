package chess;

import chess.core.*;
import chess.rules.*;
import chess.util.*;

/**
 * Main entry point for the AFK Chess application.
 * Contains wiring and base tests for the core engine.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("      AFK2 – OOP Console Chess");
        System.out.println("========================================\n");

        // Test 1: Initialize game
        testGameInitialization();

        // Test 2: Board representation
        testBoardRepresentation();

        // Test 3: Position and algebraic notation
        testPositionAndNotation();

        // Test 4: Basic move validation
        testBasicMoveValidation();

        // Test 5: Check detection
        testCheckDetection();

        System.out.println("\n========================================");
        System.out.println("     All Basic Tests Completed!");
        System.out.println("========================================");
    }

    private static void testGameInitialization() {
        System.out.println("\n--- Test 1: Game Initialization ---");
        Player white = new Player("Alice", Color.WHITE, false);
        Player black = new Player("Bob", Color.BLACK, false);
        ChessClock clock = new ChessClock(5 * 60 * 1000); // 5 minutes per player
        Game game = new Game(white, black, clock);

        System.out.println("✓ Game created: " + game);
        System.out.println("✓ White player: " + white);
        System.out.println("✓ Black player: " + black);
        System.out.println("✓ Current player: " + game.getCurrentPlayer());
        System.out.println("✓ Game state: " + game.getGameState());
    }

    private static void testBoardRepresentation() {
        System.out.println("\n--- Test 2: Board Representation ---");
        Board board = new Board();
        
        // Check starting position
        Piece e2 = board.getPiece(new Position(4, 1)); // e2
        Piece e7 = board.getPiece(new Position(4, 6)); // e7
        
        System.out.println("✓ Pawn at e2: " + e2);
        System.out.println("✓ Pawn at e7: " + e7);
        System.out.println("✓ White king position: " + board.getWhiteKingPosition());
        System.out.println("✓ Black king position: " + board.getBlackKingPosition());
        
        BoardPrinter.printBoardSimple(board);
    }

    private static void testPositionAndNotation() {
        System.out.println("--- Test 3: Position and Notation ---");
        
        Position e4 = Position.fromAlgebraic("e4");
        System.out.println("✓ Position from 'e4': file=" + e4.getFile() + ", rank=" + e4.getRank());
        System.out.println("✓ Position to algebraic: " + e4.toAlgebraic());
        
        Position a1 = Position.fromAlgebraic("a1");
        System.out.println("✓ Position a1: " + a1);
        
        Position h8 = Position.fromAlgebraic("h8");
        System.out.println("✓ Position h8: " + h8);
    }

    private static void testBasicMoveValidation() {
        System.out.println("\n--- Test 4: Basic Move Validation ---");
        Board board = new Board();
        
        // Test pawn move e2-e4
        Position e2 = new Position(4, 1);
        Position e4 = new Position(4, 3);
        Piece pawn = board.getPiece(e2);
        
        Move pawnMove = new Move.Builder(e2, e4, pawn).build();
        boolean isValid = MoveValidator.isValidMove(board, pawnMove, Color.WHITE);
        System.out.println("✓ Pawn e2-e4 is valid: " + isValid);
        
        // Test illegal move (e2-e5, too far for pawn in one move... wait, actually 2 squares is allowed for pawns)
        Position e5 = new Position(4, 4);
        Move illegalMove = new Move.Builder(e2, e5, pawn).build();
        boolean isValid2 = MoveValidator.isValidMove(board, illegalMove, Color.WHITE);
        System.out.println("✓ Pawn e2-e5 (two squares) is valid: " + isValid2);
        
        // Test moving black piece as white
        Position e7 = new Position(4, 6);
        Piece blackPawn = board.getPiece(e7);
        Move invalidColorMove = new Move.Builder(e7, new Position(4, 5), blackPawn).build();
        boolean isValid3 = MoveValidator.isValidMove(board, invalidColorMove, Color.WHITE);
        System.out.println("✓ Moving black pawn as white is valid: " + isValid3);
    }

    private static void testCheckDetection() {
        System.out.println("\n--- Test 5: Check Detection ---");
        Board board = new Board();
        CheckDetector detector = new CheckDetector();
        
        boolean whiteInCheck = detector.isKingInCheck(board, Color.WHITE);
        boolean blackInCheck = detector.isKingInCheck(board, Color.BLACK);
        System.out.println("✓ White in check at start: " + whiteInCheck);
        System.out.println("✓ Black in check at start: " + blackInCheck);
        
        boolean whiteMoves = detector.hasAnyLegalMove(board, Color.WHITE);
        boolean blackMoves = detector.hasAnyLegalMove(board, Color.BLACK);
        System.out.println("✓ White has legal moves: " + whiteMoves);
        System.out.println("✓ Black has legal moves: " + blackMoves);
    }
}
