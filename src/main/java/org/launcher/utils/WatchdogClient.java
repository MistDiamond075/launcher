package org.launcher.utils;

import javafx.animation.AnimationTimer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WatchdogClient {
    private static final Path PIPE = Path.of("\\\\.\\pipe\\LauncherWatchdog");
    private static final long FX_TIMEOUT = TimeUnit.SECONDS.toNanos(5);
    private static final byte CMD_HEARTBEAT = 1;
    private static final byte CMD_EXIT = 2;
    private static volatile long lastFxTick;

    private static OutputStream out;

    private static final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Watchdog");
                t.setDaemon(true);
                return t;
            });

    private static AnimationTimer animationTimer;

    public static void start() {
        lastFxTick = System.nanoTime();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                lastFxTick = now;
            }
        };
        animationTimer.start();
        executor.scheduleAtFixedRate(
                WatchdogClient::heartbeat,
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    private static synchronized void heartbeat() {

        if (System.nanoTime() - lastFxTick > FX_TIMEOUT) {
            return;
        }
        try {
            if (out == null) {
                out = Files.newOutputStream(PIPE);
            }
            out.write(1);
            out.flush();
        } catch (IOException e) {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
            }
            out = null;
        }
    }

    public static synchronized void shutdownWatchdog() throws IOException {
        if (out != null) {
            out.write(CMD_EXIT);
            out.flush();
        }
    }

    public static synchronized void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        executor.shutdownNow();
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException ignored) {
        }
    }
}
