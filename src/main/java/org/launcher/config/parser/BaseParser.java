package org.launcher.config.parser;

import org.launcher.exception.ConfigurationException;

import java.nio.file.Path;
import java.util.Map;

public interface BaseParser {
    Map<String,String> parse(Path path) throws ConfigurationException;
    Map<String,String> parse(String text) throws ConfigurationException;
}
