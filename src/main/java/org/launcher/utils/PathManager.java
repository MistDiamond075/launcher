package org.launcher.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathManager {
    public static Path normalize(String path) {
        if(path == null || path.isBlank()){
            return null;
        }
        if(path.contains("?appd?")){
            path = path.replace("?appd?", getAppDir().toString());
        }
        Path p = Paths.get(path);
        return p.toAbsolutePath().normalize();
    }

    public static Path getAppDir() {
        return ProcessHandle.current()
                .info()
                .command()
                .map(Paths::get)
                .map(Path::getParent)
                .orElse(null);
    }
}
