package chess.pgn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PgnWriter
{

    public void write(File file, PgnGameMetadata meta, List<PgnMoveRecord> moves) throws IOException
    {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(meta, "meta must not be null");
        Objects.requireNonNull(moves, "moves must not be null");

        try (BufferedWriter out = new BufferedWriter(new FileWriter(file)))
        {
            for (Map.Entry<String, String> e : meta.getAllTags().entrySet())
            {
                out.write(String.format("[%s \"%s\"]%n", e.getKey(), escapeTagValue(e.getValue())));
            }
            out.write("\n");

            int count = 0;
            for (PgnMoveRecord rec : moves)
            {
                if (count > 0) out.write(" ");
                out.write(rec.getMoveNumber() + ".");
                if (rec.getWhiteSan() != null) out.write(" " + rec.getWhiteSan());
                if (rec.getBlackSan() != null) out.write(" " + rec.getBlackSan());
                count++;
                if (count % 6 == 0) out.write(System.lineSeparator());
            }

            String result = meta.getTag("Result");
            if (result != null && !result.isEmpty()) {out.write(" " + result);}
            out.write(System.lineSeparator());
            out.flush();
        }
    }

    private String escapeTagValue(String v)
    {
        if (v == null) return "";
        return v.replace("\"", "\\\"");
    }
}
