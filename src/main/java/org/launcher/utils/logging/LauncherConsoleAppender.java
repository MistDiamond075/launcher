package org.launcher.utils.logging;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

public class LauncherConsoleAppender {
    public static ch.qos.logback.core.ConsoleAppender<ILoggingEvent> create(Context  context, LayoutWrappingEncoder<ILoggingEvent> encoder) {
        ch.qos.logback.core.ConsoleAppender<ILoggingEvent> appender = new ch.qos.logback.core.ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("STDOUT");
        appender.setEncoder(encoder);
        appender.start();
        return appender;
    }
}
