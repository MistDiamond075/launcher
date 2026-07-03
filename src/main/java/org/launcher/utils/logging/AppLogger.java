package org.launcher.utils.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import org.launcher.entity.ConfigurationEntity;
import org.launcher.entity.LoggingEntity;
import org.slf4j.LoggerFactory;

public class AppLogger {
    private final LoggerFormatter formatter;
    private final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    private final ch.qos.logback.classic.Logger root = context.getLogger("org.launcher");

    public AppLogger(ConfigurationEntity config) {
        formatter = new LoggerFormatter();
        formatter.setColorsEnabled(config.getLog().isColorsEnabled());
        formatter.setContext(context);
        formatter.start();
        root.setAdditive(false);
        reload(config);
    }

    public synchronized void reload(ConfigurationEntity config){
        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        root.detachAppender("FILE");
        root.detachAppender("STDOUT");
        encoder.setContext(context);
        encoder.setLayout(formatter);
        encoder.start();
        switch(config.getLog().getLevel()){
            case LoggingEntity.Level.DEBUG ->  root.setLevel(Level.DEBUG);
            case LoggingEntity.Level.WARNING ->  root.setLevel(Level.WARN);
            case LoggingEntity.Level.ERROR ->  root.setLevel(Level.ERROR);
            default ->  root.setLevel(Level.INFO);
        }
        Appender<ILoggingEvent> appender;
        switch(config.getLog().getLog()){
            case FILE -> appender = LauncherFileAppender.create(config, context, encoder);
            case DISABLED -> {
                root.setLevel(Level.OFF);
                return;
            }
            default -> appender = LauncherConsoleAppender.create(context, encoder);
        }
        root.addAppender(appender);
        //System.out.println(appender.isStarted());
        //System.out.println(encoder.isStarted());
        //StatusPrinter.print(context);
    }
}
