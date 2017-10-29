package engagement4.collab;

public class CollabRuntimeException extends RuntimeException
{
    public CollabRuntimeException(final String message) {
        super(message);
    }
    
    public CollabRuntimeException() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
