package org.launcher.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Exporter {
    private static final Path exportPath = Path.of(System.getProperty("user.home"),"launcher-logs.zip");

    public static Path getExportPath() {
        return exportPath;
    }

    public static void exportLogs(Path logsPath) throws IOException {
        makeZip(logsPath, exportPath);
    }

    public static void exportLogsRecurse(Path logsPath) throws IOException {
        makeZipRecursive(logsPath, exportPath);
    }

    private static void makeZip(Path source, Path target) throws IOException {

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(target));
             InputStream fis = Files.newInputStream(source)) {

            ZipEntry entry = new ZipEntry(source.getFileName().toString());
            zos.putNextEntry(entry);
            fis.transferTo(zos);
            zos.closeEntry();
        }
    }

    private static void makeZipRecursive(Path source, Path target) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(
                Files.newOutputStream(target))) {

            try (Stream<Path> stream = Files.walk(source)) {
                stream.filter(Files::isRegularFile)
                        .forEach(path -> {
                            ZipEntry entry = new ZipEntry(
                                    source.relativize(path).toString()
                            );

                            try {
                                zos.putNextEntry(entry);
                                try (InputStream is = Files.newInputStream(path)) {
                                    is.transferTo(zos);
                                }
                                zos.closeEntry();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }
}
