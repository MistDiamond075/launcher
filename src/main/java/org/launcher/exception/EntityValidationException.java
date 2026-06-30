package org.launcher.exception;

public class EntityValidationException extends BaseException {
    public EntityValidationException(String message) {
        super(message);
    }

    public EntityValidationException(String message,String userMessage, Throwable cause, boolean visible) {
        super(message,userMessage, cause,visible);
    }

    public EntityValidationException(String message,String userMessage, boolean visible) {
        super(message,userMessage,visible);
    }

    public EntityValidationException(Throwable cause,String userMessage, boolean visible) {
        super(cause, userMessage,visible);
    }
}
