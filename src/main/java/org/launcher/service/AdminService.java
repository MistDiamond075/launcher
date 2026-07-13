package org.launcher.service;

import javafx.application.Platform;
import org.launcher.config.ConfigurationControl;
import org.launcher.config.Localization;
import org.launcher.exception.BaseException;
import org.launcher.utils.Exporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
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
        File file = new File(path);
        try {
            return new ProcessBuilder("notepad.exe", path).start();
        } catch (IOException e) {
            NotificationService.show("admin.txt.open.fail", "Failed to open text editor",false, BaseException.Type.ERROR);
            logger.debug("Details: ",e);
            return null;
        }
    }

    public void exportLogs(){
        Path path = Path.of(configurationControl.getConfiguration().getLog().getPath());
        if(configurationControl.getConfiguration().getLog().isHistoryEnabled()){
            String prefix = Files.isDirectory(path) ? "launcher" : path.getFileName().toString();
            Pattern pattern = Pattern.compile(prefix + "-\\\\d{2}-\\\\d{2}-\\\\d{4}\\\\.log");
            try {
                Path tempDir = Files.createTempDirectory("logs-");
            try (Stream<Path> files = Files.list(path)) {
                files
                        .filter(Files::isRegularFile)
                        .filter(p -> pattern.matcher(p.getFileName().toString()).matches())
                        .forEach(p -> moveToTemp(p, tempDir));
                Exporter.exportLogsRecurse(tempDir);
            } catch (IOException e) {
                NotificationService.show("admin.txt.export.fail", "Failed to create temp directory",false, BaseException.Type.ERROR);
                logger.debug("Details: ",e);
                return;
            }
            } catch (IOException e) {
               NotificationService.show("admin.logs.export.fail", "Failed to export logs",false, BaseException.Type.ERROR);
               logger.debug("Details: ",e);
               return;
            }
        }else{
            try {
                Exporter.exportLogs(path);
            } catch (IOException e) {
                NotificationService.show("admin.logs.export.fail", "Failed to export logs",false, BaseException.Type.ERROR);
                logger.debug("Details: ",e);
                return;
            }
        }
        logger.info("Logs exported");
        NotificationService.show(
                MessageFormat.format(
                        Localization.get("admin.logs.export.success"),
                        Exporter.getExportPath()
                ),
                "Logs exported successfully",
                true,
                BaseException.Type.INFO
        );
    }

    public void startExplorer(){
        try {
            new ProcessBuilder("explorer.exe").start();
        } catch (IOException e) {
            NotificationService.show("admin.explorer.fail", "Failed to start explorer",false, BaseException.Type.ERROR);
            logger.debug("Details: ",e);
        }
    }

    public void reboot(){
        long pid = ProcessHandle.current().pid();

        ProcessHandle.current()
                .info()
                .command()
                .ifPresent(command -> {
                    try {
                        new ProcessBuilder(command).start();
                    } catch (IOException e) {
                        NotificationService.show("app.reboot.failed","Failed to reboot",false,BaseException.Type.ERROR);
                        logger.debug("Details: ",e);
                        Platform.exit();
                        System.exit(0);
                    }
                });

        Platform.exit();
        System.exit(0);
    }

    private static void moveToTemp(Path source, Path tempDir) {
        try {
            Files.move(
                    source,
                    tempDir.resolve(source.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println("Moved: " + source.getFileName());

        } catch (IOException e) {
            System.err.println("Failed to move " + source + ": " + e.getMessage());
        }
    }
}
