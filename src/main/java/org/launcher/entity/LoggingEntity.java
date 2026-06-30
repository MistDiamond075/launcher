package org.launcher.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.launcher.exception.EntityValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class LoggingEntity implements BaseEntity {
    private static final Logger logger = LoggerFactory.getLogger(LoggingEntity.class);
    public enum Level {ERROR, WARNING, INFO, DEBUG}
    public enum Log{DISABLED, CONSOLE, FILE}
    private final boolean colorsEnabled;
    private final int maxFileHistory;
    private final String path;
    private final Level level;
    private final Log log;

    @JsonCreator
    public LoggingEntity(
            @JsonProperty("colorsEnabled") boolean colorsEnabled,
            @JsonProperty("path") String path,
            @JsonProperty("level") String level,
            @JsonProperty("log") String log,
            @JsonProperty("maxFileHistory")int maxFileHistory) {
        this.colorsEnabled = colorsEnabled;
        this.path = path;
        Level lvl = null;
        try {
            lvl = Level.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("{} is not a valid level. Using INFO instead", level);
            lvl = Level.INFO;
        }finally {
            this.level = lvl;
        }
        Log logTo = null;
        try {
            logTo = Log.valueOf(log.toUpperCase());
        }catch (IllegalArgumentException e) {
            logger.warn("{} is not a valid log level. Using CONSOLE instead", log);
            logTo = Log.CONSOLE;
        }finally {
            this.log = logTo;
        }
        if(maxFileHistory <1){
            logger.warn("maxFileHistory should be above 0. Setting to 1 as default");
            this.maxFileHistory = 1;
        }else{
            this.maxFileHistory = maxFileHistory;
        }
    }

    public boolean isColorsEnabled() {
        return colorsEnabled;
    }

    public String getPath() {
        return path;
    }

    public Level getLevel() {
        return level;
    }

    public Log getLog() {
        return log;
    }

    public int getMaxFileHistory() {
        return maxFileHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LoggingEntity that = (LoggingEntity) o;
        return level == that.level && log == that.log;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, log);
    }

    @Override
    public String toString() {
        return "LoggingEntity{" +
                ", filePath='" + path + '\'' +
                ", level=" + level +
                ", log=" + log +
                '}';
    }

    @Override
    public void validate() throws EntityValidationException {
        if(this.path ==null){
            throw new EntityValidationException("filePath is null");
        }
        if(!Files.isDirectory(Path.of(this.path))){
            if(!Files.exists(Path.of(this.path))){
                logger.warn("filePath {} does not exist. Creating file", this.path);
                File logfile = new File(this.path);
                try {
                    if (logfile.createNewFile()) {
                        logger.debug("File created");
                    }else{
                        logger.debug("File already exists");
                    }
                } catch (IOException e) {
                    throw new EntityValidationException("Failed to create file "+ this.path);
                }
            }
        }
        if(!Files.exists(Path.of(this.path))){
            throw new EntityValidationException("filePath "+ path +" does not exist");
        }
    }
}
