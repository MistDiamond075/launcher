package org.launcher.async;

import javafx.application.Platform;
import org.launcher.entity.InstanceEntity;
import org.launcher.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CloseWindowAsync {
    private static final Logger logger = LoggerFactory.getLogger(CloseWindowAsync.class);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Pair<Long,TimeUnit> defaultDelay = new Pair<>(150L, TimeUnit.MILLISECONDS);
    private static final Pair<Long,TimeUnit> defaultPeriod = new Pair<>(5000L, TimeUnit.MILLISECONDS);

    public static void scheduleClose(final InstanceEntity instance, final long delay,final long period, final TimeUnit unit) {
        AtomicReference<ScheduledFuture<?>> ref = new AtomicReference<>();

        ref.set(scheduler.scheduleAtFixedRate(() -> {
            logger.debug("Trying to close window, hwnds={}", instance.getHwnds());
            if (instance.getHwnds().isEmpty()) {
                Platform.runLater(() -> instance.setState(InstanceEntity.State.CLOSED));

                ref.get().cancel(false);
            }
        }, delay, period, unit));
    }

    public static void scheduleClose(final InstanceEntity instance) {
        scheduleClose(instance,defaultDelay.key, defaultPeriod.key, defaultDelay.value);
    }

    public static void stop() {
        try {
            logger.debug("Stopping CloseWindow scheduler");
            scheduler.shutdown();
            boolean off = scheduler.awaitTermination(5, TimeUnit.SECONDS);
            if(!off){
                throw new InterruptedException();
            }
        } catch (InterruptedException e) {
            logger.debug("Stopping scheduler immediately");
            scheduler.shutdownNow();
        }
        logger.debug("CloseWindow scheduler stopped");
    }
}
