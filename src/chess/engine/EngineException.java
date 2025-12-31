package chess.engine;

/**
 * Custom exception thrown when an error occurs in the chess engine.
 * Used for engine-related errors such as process failures, communication issues, etc.
 */
public class EngineException extends Exception {
    /**
     * Creates an EngineException with a detail message.
     * 
     * @param message the detail message
     */
    public EngineException(String message) { 
        super(message); 
    }
    
    /**
     * Creates an EngineException with a detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public EngineException(String message, Throwable cause) { 
        super(message, cause); 
    }
}
