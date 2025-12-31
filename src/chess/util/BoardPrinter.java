package chess.util;

import chess.core.*;

/**
 * Utility class for printing the chess board to console.
 */
public class BoardPrinter {

    private BoardPrinter() {
        // Prevent instantiation
    }

    /**
     * Prints the board in a human-readable format.
     */
    public static void printBoard(Board board) {
        if (board == null) {
            System.out.println("Board is null");
            return;
        }

        System.out.println("\n   a b c d e f g h");
        System.out.println("  ┌─────────────────┐");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " │");
            for (int file = 0; file < 8; file++) {
                Position pos = new Position(file, rank);
                Piece piece = board.getPiece(pos);
                
                char symbol = piece != null ? piece.getSymbol() : '·';
                System.out.print(" " + symbol);
            }
            System.out.println(" │ " + (rank + 1));
        }

        System.out.println("  └─────────────────┘");
        System.out.println("   a b c d e f g h\n");
    }

    /**
     * Prints a simplified board without borders for compact display.
     * Displays the board as a grid with dots for empty squares and piece symbols.
     * Useful for logging or simple console output.
     * 
     * @param board the board to print
     */
    public static void printBoardSimple(Board board) {
        if (board == null) {
            System.out.println("Board is null");
            return;
        }

        System.out.println();
        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                Position pos = new Position(file, rank);
                Piece piece = board.getPiece(pos);
                
                char symbol = piece != null ? piece.getSymbol() : '.';
                System.out.print(symbol + " ");
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h\n");
    }

    /**
     * Gets a string representation of the board without printing to console.
     * Returns the board visualization as a formatted string for logging or other uses.
     * 
     * @param board the board to convert to string
     * @return formatted string representation of the board
     */
    public static String getBoardAsString(Board board) {
        if (board == null) {
            return "Board is null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n   a b c d e f g h\n");
        sb.append("  ┌─────────────────┐\n");

        for (int rank = 7; rank >= 0; rank--) {
            sb.append((rank + 1)).append(" │");
            for (int file = 0; file < 8; file++) {
                Position pos = new Position(file, rank);
                Piece piece = board.getPiece(pos);
                
                char symbol = piece != null ? piece.getSymbol() : '·';
                sb.append(" ").append(symbol);
            }
            sb.append(" │ ").append((rank + 1)).append("\n");
        }

        sb.append("  └─────────────────┘\n");
        sb.append("   a b c d e f g h\n");

        return sb.toString();
    }

    /**
     * Prints the board with a specific position highlighted for visual reference.
     * Displays brackets around the piece at the specified position.
     * Useful for showing the current move or selected piece.
     * 
     * @param board the board to print
     * @param highlightPosition the position to highlight, or null for no highlighting
     */
    public static void printBoardWithHighlight(Board board, Position highlightPosition) {
        if (board == null) {
            System.out.println("Board is null");
            return;
        }

        System.out.println("\n   a b c d e f g h");
        System.out.println("  ┌─────────────────┐");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " │");
            for (int file = 0; file < 8; file++) {
                Position pos = new Position(file, rank);
                Piece piece = board.getPiece(pos);
                
                char symbol = piece != null ? piece.getSymbol() : '·';
                
                if (highlightPosition != null && highlightPosition.equals(pos)) {
                    System.out.print(" " + "[" + symbol + "]");
                } else {
                    System.out.print(" " + symbol);
                }
            }
            System.out.println(" │ " + (rank + 1));
        }

        System.out.println("  └─────────────────┘");
        System.out.println("   a b c d e f g h\n");
    }
}
