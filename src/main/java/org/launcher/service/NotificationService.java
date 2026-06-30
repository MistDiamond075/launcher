package org.launcher.service;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.launcher.config.Localization;
import org.launcher.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final ScheduledExecutorService systemMessageExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final long default_delay = 5L;
    private static HBox container;
    private static Label label;

    public static void initialize(HBox c, Label l) {
        container = c;
        label = l;
        c.setVisible(false);
    }

    public static void show(String message,String defaultMessage, Long delay, BaseException.Type type) {
        if(container!=null && label!=null) {
            Platform.runLater(() -> {
                String msg = Localization.get(message, defaultMessage);
               // label.setText(msg);
                container.setVisible(true);
                label.getStyleClass().removeAll(
                        "system-message-info",
                        "system-message-warning",
                        "system-message-error",
                        "system-message-neutral"
                );
                switch (type) {
                    case INFO -> {
                        label.setText("√ "+msg);
                        label.getStyleClass().add("system-message-info");
                    }
                    case WARNING -> {
                        label.setText("‼ "+msg);
                        label.getStyleClass().add("system-message-warning");
                    }
                    case ERROR -> {
                        label.setText("x "+msg);
                        label.getStyleClass().add("system-message-error");
                    }
                    default -> label.getStyleClass().add("system-message-neutral");
                }
                systemMessageExecutor.schedule(() -> {
                    container.setVisible(false);
                }, delay, TimeUnit.SECONDS);
            });
        }
    }

    public static void show(String message,String defaultMessage, BaseException.Type type) {
        show(message,defaultMessage, default_delay, type);
    }

    public static void show(BaseException ex) {
        show(ex.getUserMessage(),"Undefined error", BaseException.Type.ERROR);
    }

    public static void stopExecutor() {
        logger.debug("Stopping systemMessage executor");
        systemMessageExecutor.shutdown();
        try {
            if(!systemMessageExecutor.awaitTermination(5L,TimeUnit.SECONDS)){
                systemMessageExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
