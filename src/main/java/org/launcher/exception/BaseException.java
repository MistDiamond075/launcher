package org.launcher.exception;

public class BaseException extends Exception {
    public enum Type{ERROR, WARNING}
    Type type;

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message,Throwable cause) {
        super(message,cause);
    }
    public BaseException(String message, Type type) {
        super(message);
        this.type = type;
    }

    public BaseException(Throwable cause, Type type) {
        super(cause);
        this.type = type;
    }
}
