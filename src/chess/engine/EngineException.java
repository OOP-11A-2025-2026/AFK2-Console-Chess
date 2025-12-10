package chess.engine;

public class EngineException extends Exception
{
    public EngineException(String message) { super(message); }
    public EngineException(String message, Throwable cause) { super(message, cause); }
}
