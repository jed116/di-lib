package tech.itpark.exception;

// alt + enter
public class DException extends RuntimeException {
    public DException() {
        super();
    }

    public DException(String message) {
        super(message);
    }

    public DException(String message, Throwable cause) {
        super(message, cause);
    }

    public DException(Throwable cause) {
        super(cause);
    }

    protected DException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
