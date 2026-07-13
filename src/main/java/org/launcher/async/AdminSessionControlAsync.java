package org.launcher.async;

import javafx.scene.layout.HBox;
import org.launcher.MainApp;
import org.launcher.config.ConfigurationControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AdminSessionControlAsync {
    private static final Logger logger = LoggerFactory.getLogger(AdminSessionControlAsync.class);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ConfigurationControl configurationControl;
    private static ScheduledFuture<?> scheduledFuture = null;
    private static MainApp app = null;

    public static void initialize(ConfigurationControl conf, MainApp ma) {
        configurationControl = conf;
        app = ma;
    }

    public static void start() {
        int timeout = configurationControl.getConfiguration().getAdmin().getSessionTimeout();
        if(timeout > 0) {
             scheduledFuture = scheduler.schedule(() ->{
                 app.reloadScene(null);
                logger.warn("Admin session timeout");
                scheduledFuture.cancel(true);
                scheduledFuture = null;
            },timeout, TimeUnit.MINUTES);
        }
    }

    public static void delayTermination() {
        if(scheduledFuture != null) {
            scheduledFuture.cancel(true);
            start();
        }
    }

    public static void stop() {
        logger.info("Stopping Admin session scheduler");
        if(scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        scheduler.shutdown();
        try {
            if(!scheduler.awaitTermination(5L,TimeUnit.SECONDS)){
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
