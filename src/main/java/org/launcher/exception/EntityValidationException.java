package org.launcher.exception;

public class EntityValidationException extends BaseException {
    public EntityValidationException(String message) {
        super(message);
    }

    public EntityValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityValidationException(String message, Type type) {
        super(message, type);
    }

    public EntityValidationException(Throwable cause, Type type) {
        super(cause, type);
    }
}
