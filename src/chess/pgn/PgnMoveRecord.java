package chess.pgn;

import java.util.Objects;

public final class PgnMoveRecord
{
    private final int moveNumber;
    private final String whiteSan;
    private final String blackSan;

    public PgnMoveRecord(int moveNumber, String whiteSan, String blackSan)
    {
        if (moveNumber <= 0) throw new IllegalArgumentException("moveNumber must be >= 1");
        this.moveNumber = moveNumber;
        this.whiteSan = whiteSan != null ? whiteSan.trim() : null;
        this.blackSan = blackSan != null ? blackSan.trim() : null;
    }

    public int getMoveNumber() {return moveNumber;}

    public String getWhiteSan() {return whiteSan;}

    public String getBlackSan() {return blackSan;}

    public String toString()
    {
        if (blackSan == null) {return moveNumber + ". " + (whiteSan == null ? "?" : whiteSan);}
        else {return moveNumber + ". " + (whiteSan == null ? "?" : whiteSan) + " " + blackSan;}
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof PgnMoveRecord)) return false;
        PgnMoveRecord other = (PgnMoveRecord) o;
        return moveNumber == other.moveNumber
                && Objects.equals(whiteSan, other.whiteSan)
                && Objects.equals(blackSan, other.blackSan);
    }

    public int hashCode() {return Objects.hash(moveNumber, whiteSan, blackSan);}
}

