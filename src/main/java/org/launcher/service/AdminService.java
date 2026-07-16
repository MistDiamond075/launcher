package org.launcher.service;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import org.launcher.config.ConfigurationControl;
import org.launcher.config.Localization;
import org.launcher.exception.BaseException;
import org.launcher.utils.Exporter;
import org.launcher.utils.WatchdogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final ConfigurationControl configurationControl;

    public AdminService(final ConfigurationControl configurationControl) {
        this.configurationControl = configurationControl;
    }

    public Process openTextEditor(String path) {
        try {
            logger.info("Starting text editor for {}", path);
            return new ProcessBuilder("notepad.exe", path).start();
        } catch (IOException e) {
            NotificationService.show("admin.txt.open.fail", "Failed to open text editor",false, BaseException.Type.ERROR);
            logger.error("Failed to open text editor");
            logger.debug("Details: ",e);
            return null;
        }
    }

    public void exportLogs() {
        try {
            Path path = Path.of(configurationControl.getConfiguration().getLog().getPath());
            if (configurationControl.getConfiguration().getLog().isHistoryEnabled()) {
                String prefix = Files.isDirectory(path) ? "launcher" : path.getFileName().toString();
                Pattern pattern = Pattern.compile(Pattern.quote(prefix) + "-\\\\d{2}-\\\\d{2}-\\\\d{4}\\\\.log");

                Path tempDir = Files.createTempDirectory("logs-");
                try (Stream<Path> files = Files.list(path)) {
                    List<Path> fileList = files
                            .filter(Files::isRegularFile)
                            .filter(p -> pattern.matcher(p.getFileName().toString()).matches())
                            .toList();
                    int errors = 0;
                    for (Path f : fileList) {
                        try {
                            moveToTemp(f, tempDir);
                        }catch (IOException e) {
                            logger.error("Failed to move log file {}",f);
                            logger.debug("Details: ",e);
                            errors++;
                        }
                    }
                    if(errors == fileList.size()) {
                        throw new IOException("Failed to move log files");
                    }
                    Exporter.exportLogsRecurse(tempDir);
                }finally {
                    Files.deleteIfExists(tempDir);
                }
            } else {
                Exporter.exportLogs(path);
            }
            logger.info("Logs exported to {}", Exporter.getExportPath());
            NotificationService.show(
                    MessageFormat.format(
                            Localization.get("admin.logs.export.success"),
                            Exporter.getExportPath()
                    ),
                    "Logs exported successfully",
                    true,
                    BaseException.Type.INFO
            );
        } catch (IOException e) {
            NotificationService.show("admin.logs.export.fail", "Failed to export logs", false, BaseException.Type.ERROR);
            logger.error("Failed to export logs");
            logger.debug("Details: ", e);
        }
    }

    public void startExplorer(){
        try {
            logger.info("Starting explorer");
            new ProcessBuilder("explorer.exe").start();
        } catch (IOException e) {
            NotificationService.show("admin.explorer.fail", "Failed to start explorer",false, BaseException.Type.ERROR);
            logger.error("Failed to start explorer");
            logger.debug("Details: ",e);
        }
    }

    public void shutdown(){
        logger.info("Shutting down");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        try {
            WatchdogClient.shutdownWatchdog();
        } catch (IOException ex) {
            logger.error("Failed to shutdown watchdog");
            logger.debug("Details: ",ex);
        }
        pause.setOnFinished(event -> Platform.exit());
        pause.play();
    }

    public void reboot(){
        logger.info("Rebooting");
        Platform.exit();
    }

    private static void moveToTemp(Path source, Path tempDir) throws IOException {
        Files.move(
                source,
                tempDir.resolve(source.getFileName()),
                StandardCopyOption.REPLACE_EXISTING
        );
        logger.debug("Log file {} moved to {}", source, tempDir);
    }
}
