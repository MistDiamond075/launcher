package org.launcher.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathManager {
    public static Path normalize(String path) {
        if(path == null || path.isBlank()){
            return null;
        }
        Path p = Paths.get(path);
        return p.toAbsolutePath().normalize();
    }
}
