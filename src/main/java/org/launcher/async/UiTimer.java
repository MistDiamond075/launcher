package org.launcher.async;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UiTimer {
    private static final Logger logger = LoggerFactory.getLogger(UiTimer.class);
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledFuture = null;

    public static void start(Label l){
        if(scheduledFuture == null) {
            scheduledFuture = executor.scheduleAtFixedRate(() -> {
                Platform.runLater(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    l.setText(formatDateTime(now));
                });
            }, 0, 1L, TimeUnit.SECONDS);
        }
    }

    public static void stop(){
        logger.debug("Stopping UI Timer");
        scheduledFuture.cancel(true);
        executor.shutdown();
        try {
            if(!executor.awaitTermination(5L,TimeUnit.SECONDS)){
                logger.debug("Forcing ui timer to stop");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String formatDateTime(LocalDateTime date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, dd.MM.yyyy HH:mm:ss");
        return date.format(formatter);
    }
}
