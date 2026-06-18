package org.launcher.config.parser;

import java.nio.file.Path;
import java.util.Map;

public class JsonParser implements BaseParser {
    @Override
    public Map<String, String> parse(String text) {
        return Map.of();
    }

    @Override
    public Map<String, String> parse(Path path) {
        return Map.of();
    }
}
