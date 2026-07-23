package org.launcher.async;

import org.launcher.MainApp;
import org.launcher.config.ConfigurationControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class SessionControlAsync {
    public enum SessionType {USER, ADMIN}
    private static final Logger logger = LoggerFactory.getLogger(SessionControlAsync.class);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ConfigurationControl configurationControl;
    private static ScheduledFuture<?> sessionFuture = null;
    private static MainApp app = null;
    private static SessionType currentSession = SessionType.USER;

    public static void initialize(ConfigurationControl conf, MainApp ma) {
        configurationControl = conf;
        app = ma;
    }

    public static void start(SessionType type) {
        try{
            SessionType.valueOf(type.name());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid session type: {}", type.name());
            return;
        }
        Integer timeout = type == SessionType.ADMIN ?
                configurationControl.getConfiguration().getAdmin().getSessionTimeout() :
                configurationControl.getConfiguration().getLauncher().getSessionTimeout();
        if(timeout != null) {
            currentSession = type;
            sessionFuture = scheduler.schedule(() ->{
                 app.reloadScene(null);
                logger.warn("{} session timeout", type);
                sessionFuture.cancel(true);
            },timeout, TimeUnit.MINUTES);
        }
    }

    public static void delayTermination() {
        if(sessionFuture != null) {
            //logger.info("Session termination was cancelled");
            sessionFuture.cancel(true);
            start(currentSession);
        }
    }

    public static void cancel() {
        if(sessionFuture != null) {
            logger.debug("Cancelling session with type {}", currentSession);
            sessionFuture.cancel(true);
        }else{
            logger.error("No session found for session type {}", currentSession);
        }
    }

    public static void stop() {
        logger.debug("Stopping Admin session scheduler");
       if(sessionFuture != null) {
           sessionFuture.cancel(true);
       }
        scheduler.shutdown();
        try {
            if(!scheduler.awaitTermination(5L,TimeUnit.SECONDS)){
                logger.debug("Forcing stop admin session scheduler");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
