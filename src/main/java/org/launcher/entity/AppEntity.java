package org.launcher.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.launcher.exception.EntityValidationException;
import org.launcher.utils.PathManager;
import org.launcher.utils.icons.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class AppEntity implements BaseEntity{
    private static final Logger logger = LoggerFactory.getLogger(AppEntity.class);
    private final String id;
    private final String name;
    private final String description;
    private Path icon;
    private final Path path;
    private final List<String> arguments;
    private final Path workingDirectory;
    private boolean enabled;
    private final boolean allowMultipleInstances;
    private final boolean enableInstancesCounter;
    private final boolean restartOnError;
    private final Integer maxRuntime;

    @JsonCreator
    public AppEntity(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("icon") String icon,
            @JsonProperty("path") String path,
            @JsonProperty("arguments") List<String> arguments,
            @JsonProperty("workingDirectory") String workingDirectory,
            @JsonProperty("enabled") boolean enabled,
            @JsonProperty("allowMultipleInstances") boolean allowMultipleInstances,
            @JsonProperty("enableInstancesCounter") boolean enableInstancesCounter,
            @JsonProperty("restartOnError") boolean restartOnError,
            @JsonProperty("maxRuntime") Integer maxRuntime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = PathManager.normalize(icon);
        this.path = PathManager.normalize(path);
        this.arguments = arguments;
        this.workingDirectory = PathManager.normalize(workingDirectory);
        this.enabled = enabled;
        this.allowMultipleInstances = allowMultipleInstances;
        this.enableInstancesCounter = enableInstancesCounter;
        this.restartOnError = restartOnError;
        this.maxRuntime = maxRuntime;
    }

    public AppEntity(String name,String id,String icon,boolean enabled,boolean allowMultipleInstances,boolean enableInstancesCounter,boolean restartOnError,Integer maxRuntime){
        this.id = id;
        this.name = name;
        this.icon = PathManager.normalize(icon);
        this.enabled = enabled;
        this.allowMultipleInstances = allowMultipleInstances;
        this.enableInstancesCounter = enableInstancesCounter;
        this.restartOnError = restartOnError;
        this.maxRuntime = maxRuntime;
        this.workingDirectory = null;
        this.arguments = null;
        this.path = null;
        this.description = null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Path getIcon() {
        return icon;
    }

    public Path getPath() {
        return path;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAllowMultipleInstances() {
        return allowMultipleInstances;
    }

    public boolean isEnableInstancesCounter() {
        return enableInstancesCounter;
    }

    public boolean isRestartOnError() {
        return restartOnError;
    }

    public Integer getMaxRuntime() {
        return maxRuntime;
    }

    @JsonProperty("path")
    public String getPathString() {
        return path == null ? null : path.toString();
    }

    @JsonProperty("icon")
    public String getIconPathString() {
        return icon == null ? null : icon.toString();
    }

    @JsonProperty("workingDirectory")
    public String getWorkingDirectoryPathString() {
        return workingDirectory == null ? null : workingDirectory.toString();
    }

    @Override
    public void validate() throws EntityValidationException {
        if(this.path==null || this.workingDirectory==null){
            //throw new EntityValidationException("path and workingDirectory are required");
            logger.error("path and workingDirectory are required for {}", id);
            this.enabled = false;
        }else if(!Files.exists(this.path) || Files.isDirectory(this.path)){
            // throw new EntityValidationException("App "+this.path+" not found");
            logger.error("App {} not found", this.path);
            this.enabled = false;
        }else if(!Files.exists(this.workingDirectory) || !Files.isDirectory(this.workingDirectory)){
           // throw new EntityValidationException("Working directory "+this.workingDirectory+" for "+this.path+" not found");
            logger.error("Working directory {} for {} not found", this.workingDirectory, this.path);
        }else if(icon!=null && !Files.exists(this.icon)){
            //throw new EntityValidationException("Icon "+this.icon+" for "+this.path+" not found");
            logger.error("Icon {} for {} not found",this.icon,this.path);
            this.icon = null;
        }else if(icon!=null && Files.isDirectory(this.icon)){
            logger.error("Icon {} for {} is a directory",this.icon,this.path);
            this.icon = null;
        }else if(icon!=null){
            try {
                boolean isImage = ImageIO.read(icon.toFile()) != null;
                if(!isImage){
                    this.icon = null;
                }
            } catch (IOException e) {
                logger.error("Error reading icon {}", icon, e);
                this.icon = null;
            }
        }
        if(this.icon == null){
            if(this.path == null){
                return;
            }
            try {
                String ico = IconManager.make(String.valueOf(path));
                icon = ico != null ? PathManager.normalize(ico) : null;
            } catch (Exception e) {
                icon = null;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AppEntity appEntity = (AppEntity) o;
        return Objects.equals(id, appEntity.id) && Objects.equals(path, appEntity.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path);
    }

    @Override
    public String toString() {
        return "App{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                ", path='" + path + '\'' +
                ", arguments='" + arguments + '\'' +
                ", workingDirectory='" + workingDirectory + '\'' +
                ", enabled=" + enabled +
                ", allowMultipleInstances=" + allowMultipleInstances +
                ", restartOnError=" + restartOnError +
                ", maxRuntime=" + maxRuntime +
                '}';
    }
}
