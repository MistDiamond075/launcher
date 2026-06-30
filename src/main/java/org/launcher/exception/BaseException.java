package org.launcher.exception;

public class BaseException extends Exception {
    public enum Type{INFO,WARNING,ERROR}
    private boolean visible = false;
    private String message;
    private String userMessage;

    public BaseException(String message) {
        super(message);
        this.message = message;
    }

    public BaseException(String message,String userMessage,Throwable cause, boolean visible) {
        super(message,cause);
        this.visible = visible;
        this.userMessage = userMessage;
        this.message = message;
    }
    public BaseException(String message,String userMessage, boolean visible) {
        super(message);
        this.visible = visible;
        this.userMessage = userMessage;
        this.message = message;
    }

    public BaseException(Throwable cause,String userMessage,  boolean visible) {
        super(cause);
        this.visible = visible;
        this.userMessage = userMessage;
        this.message = cause.getMessage();
    }

    public boolean isVisible() {
        return visible;
    }

    public String getUserMessage() {
        return userMessage;
    }

    @Override
    public String toString() {
        return "BaseException{" +
                "visible=" + visible +
                ", message='" + message + '\'' +
                '}';
    }
}
