package org.launcher.exception;

public class ConfigurationException extends BaseException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message,cause);
    }

    public ConfigurationException(String message, Type type) {
        super(message, type);
    }

    public ConfigurationException(Throwable cause, Type type) {
        super(cause, type);
    }
}
