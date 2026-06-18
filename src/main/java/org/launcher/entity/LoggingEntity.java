package org.launcher.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.launcher.exception.EntityValidationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class LoggingEntity implements BaseEntity {
    public enum Level {ERROR, WARNING, INFO, DEBUG}
    public enum Log{DISABLED, CONSOLE, FILE}
    private final boolean printToFile;
    private final String filePath;
    private final Level level;
    private final Log log;

    @JsonCreator
    public LoggingEntity(
            @JsonProperty("printToFile") boolean printToFile,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("level") Level level,
            @JsonProperty("log") Log log) throws EntityValidationException {
        this.printToFile = printToFile;
        this.filePath = filePath;
        this.level = level;
        this.log = log;
        validate();
    }

    public boolean isPrintToFile() {
        return printToFile;
    }

    public String getFilePath() {
        return filePath;
    }

    public Level getLevel() {
        return level;
    }

    public Log getLog() {
        return log;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LoggingEntity that = (LoggingEntity) o;
        return printToFile == that.printToFile && level == that.level && log == that.log;
    }

    @Override
    public int hashCode() {
        return Objects.hash(printToFile, level, log);
    }

    @Override
    public String toString() {
        return "LoggingEntity{" +
                "printToFile=" + printToFile +
                ", filePath='" + filePath + '\'' +
                ", level=" + level +
                ", log=" + log +
                '}';
    }

    @Override
    public void validate() throws EntityValidationException {
        if(this.filePath==null){
            throw new EntityValidationException("filePath is null");
        }
        if(!Files.exists(Path.of(this.filePath))){
            throw new EntityValidationException("filePath "+filePath+" does not exist");
        }
    }
}
