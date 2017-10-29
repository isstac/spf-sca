package engagement4.collab;

class DuplicateKeyException extends Exception
{
    public DuplicateKeyException() {
    }
    
    public DuplicateKeyException(final String message) {
        super(message);
    }
}
