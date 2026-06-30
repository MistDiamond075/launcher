package org.launcher.utils.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.launcher.entity.ConfigurationEntity;

import java.nio.file.Files;
import java.nio.file.Path;

public class LauncherFileAppender {
    public static Appender<ILoggingEvent> create(ConfigurationEntity config, Context context, LayoutWrappingEncoder<ILoggingEvent> encoder) {
        if(config.getLog().getMaxFileHistory() > 1) {
            RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
            appender.setContext(context);
            appender.setName("FILE");
            String path = config.getLog().getPath();
            if(Files.isDirectory(Path.of(path))){
                path += "/application.log";
            }
            appender.setFile(path);
            appender.setAppend(true);
            setRotationPolicy(config,context, appender);
            appender.setEncoder(encoder);
            appender.start();
            return appender;
        }else{
            FileAppender<ILoggingEvent> appender = new FileAppender<>();
            appender.setContext(context);
            appender.setName("FILE");
            appender.setAppend(true);
            appender.setEncoder(encoder);
            appender.start();
            return appender;
        }
    }

    private static void setRotationPolicy(ConfigurationEntity config, Context context, RollingFileAppender<ILoggingEvent> appender) {
        TimeBasedRollingPolicy<ILoggingEvent> policy = new TimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        String name = Path.of(config.getLog().getPath()).getFileName().toString();
        String path = Files.isDirectory(Path.of(config.getLog().getPath())) ?
                Path.of(config.getLog().getPath()).getParent().toString() + "/application-%d{dd-MM-yyyy}.log" :
                name.replaceFirst("[.][^.]+$","") + "-%d{dd-MM-yyyy}.log";
        policy.setFileNamePattern(path);
        policy.setMaxHistory(config.getLog().getMaxFileHistory());
        policy.start();
        appender.setRollingPolicy(policy);
        appender.setTriggeringPolicy(policy);
    }
}
//доделать логирование в файл
//доделать пользовательские уведомления
//продолжить работу с исключениями