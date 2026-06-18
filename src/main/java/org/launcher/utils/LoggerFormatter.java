package org.launcher.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.LayoutBase;
import org.launcher.utils.colors.Colors;
import org.launcher.utils.colors.ColorsFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerFormatter extends LayoutBase<ILoggingEvent> {
    private Colors colors;

    public LoggerFormatter(){
        colors = ColorsFactory.getColors("ansi");
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        String color;
        DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm:ss VV");
        switch (event.getLevel().levelStr) {
            case "ERROR" -> color = colors.RED();
            case "WARN"  -> color = colors.YELLOW();
            case "INFO"  -> color = colors.GREEN();
            case "DEBUG" -> color = colors.PURPLE();
            default      -> color = colors.RESET();
        }

        Instant instant = Instant.ofEpochMilli(event.getTimeStamp());
        String timestamp = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                .format(TIME_FORMAT);

        StringBuilder sb = new StringBuilder();

        String loggerName = shortenLoggerName(event.getLoggerName());

        sb.append(colors.CYAN())
                .append("[").append(timestamp).append("] ")
                .append(colors.RESET())
                .append(color)
                .append("[").append(event.getLevel()).append("] ")
                .append(colors.RESET())
                .append(colors.BLUE())
                .append(loggerName)
                .append(colors.RESET())
                .append(colors.WHITE())
                .append(" - ")
                .append(event.getFormattedMessage())
                .append(colors.RESET())
                .append("\n");

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            sb.append(colors.RED())
                    .append(ThrowableProxyUtil.asString(throwableProxy))
                    .append(colors.RESET());
        }

        return sb.toString();
    }

    private String shortenLoggerName(String loggerName) {
        return loggerName!=null ? loggerName.replace("org.launcher.","") : null;
    }
}
