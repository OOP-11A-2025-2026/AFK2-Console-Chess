package chess.engine;

import chess.core.Board;
import chess.core.Color;
import chess.core.Piece;
import chess.core.Position;
import chess.pieces.*;

public class FenUtil {

    public static String generateFEN(Board board, Color sideToMove) {
        StringBuilder fen = new StringBuilder();

        // Ranks: 8 down to 1  →  y = 7 down to 0
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;

            // Files: a..h → x = 0..7
            for (int file = 0; file < 8; file++) {
                Position pos = new Position(file, rank);
                Piece piece = board.getPiece(pos);

                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(pieceToFenChar(piece));
                }
            }

            if (emptyCount > 0) {
                fen.append(emptyCount);
            }

            if (rank > 0) {
                fen.append('/');
            }
        }

        // Side to move
        fen.append(' ');
        fen.append(sideToMove == Color.WHITE ? 'w' : 'b');

        // Simplified for now:
        // - No castling rights
        // - No en passant square
        // - Halfmove clock = 0
        // - Fullmove number = 1
        fen.append(" - - 0 1");

        return fen.toString();
    }

    private static char pieceToFenChar(chess.core.Piece piece) {
        char c;

        if (piece instanceof Pawn)        c = 'p';
        else if (piece instanceof Knight) c = 'n';
        else if (piece instanceof Bishop) c = 'b';
        else if (piece instanceof Rook)   c = 'r';
        else if (piece instanceof Queen)  c = 'q';
        else if (piece instanceof King)   c = 'k';
        else                              c = '?';

        if (piece.getColor() == Color.WHITE) {
            c = Character.toUpperCase(c);
        } else {
            c = Character.toLowerCase(c);
        }

        return c;
    }
}