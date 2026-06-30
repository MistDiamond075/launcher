package org.launcher.exception;

public class ConfigurationException extends BaseException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message,String userMessage, Throwable cause, boolean visible) {
        super(message,userMessage,cause,visible);
    }

    public ConfigurationException(String message,String userMessage, boolean visible) {
        super(message, userMessage, visible);
    }

    public ConfigurationException(Throwable cause,String userMessage, boolean visible) {
        super(cause, userMessage, visible);
    }
}
