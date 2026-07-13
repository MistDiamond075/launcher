package org.launcher.config;

import org.launcher.MainApp;
import org.launcher.entity.ConfigurationEntity;
import org.launcher.exception.BaseException;
import org.launcher.exception.EntityValidationException;
import org.launcher.service.NotificationService;
import org.launcher.utils.PathManager;
import org.launcher.utils.logging.AppLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationControl {
    public enum LoadedFrom{PARAMETER,APP_DIRECTORY,DEFAULT,FAIL}
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationControl.class);
    private boolean useDefaultConfig = false;
    private Path configPath = null;
    private ConfigurationEntity configuration;
    private AppLogger appLogger = null;
    private boolean loaded = false;
    private LoadedFrom loadedFrom = LoadedFrom.DEFAULT;

    public ConfigurationControl(String configFile) {
        try{
            if(configFile.isBlank()){
                throw new NullPointerException("Specified path to config is blank");
            }
            configPath = Paths.get(configFile);
        } catch (InvalidPathException | NullPointerException e) {
            logger.warn("Non-specified or invalid configuration file {}. Loaded default configuration instead", configFile);
            logger.debug("Details: ",e);
            useDefaultConfig = true;
        }
        boolean exists = (configPath != null && Files.exists(configPath) && !Files.isDirectory(configPath)) || useDefaultConfig;
        if(!exists) {
            logger.error("Configuration file {} does not exist", configFile);
            loaded = false;
            loadedFrom = LoadedFrom.FAIL;
            return;
        }
        reload();
        //loaded = false;
    }

    public ConfigurationEntity getConfiguration() {
        return configuration;
    }

    public void reload(){
        try {
            if(useDefaultConfig) {
                InputStream input = loadDefaultConfig();
                configuration = ObjectMapperConfiguration.getMapper().readValue(input, ConfigurationEntity.class);
                input.close();
            }else {
                configuration = ObjectMapperConfiguration.getMapper().readValue(configPath.toFile(), ConfigurationEntity.class);
            }

            loaded = true;
            if(appLogger != null){
                appLogger.reload(configuration);
            }else{
                appLogger = new AppLogger(configuration);
            }
            logger.info("Configuration reloaded");
            NotificationService.show("conf.load.success","Configuration reloaded",false, BaseException.Type.INFO);
        } catch (IOException|JacksonException e) {
            logger.error("Failed to parse configuration: {}", e.getMessage());
            logger.debug("Details: ", e);
            NotificationService.show("conf.load.error","Failed to load configuration",false, BaseException.Type.ERROR);
            loaded = false;
            loadedFrom = LoadedFrom.FAIL;
        }
        if(loaded) {
            try {
                configuration.validate();
            } catch (EntityValidationException e) {
                logger.error("Configuration invalid: {}", e.getMessage());
                logger.debug("Details: ", e);
                NotificationService.show("conf.load.error", "Failed to load configuration",false, BaseException.Type.ERROR);
                loaded = false;
                loadedFrom = LoadedFrom.FAIL;
            }
        }
        if(useDefaultConfig) {
            loadedFrom = LoadedFrom.DEFAULT;
        }
    }

    public void writeNewPassword(){
        if(configuration == null) {
            return;
        }
        ObjectMapperConfiguration.getMapper().writeValue(configPath,configuration);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public LoadedFrom getLoadedFrom() {
        return loadedFrom;
    }

    public Path getConfigPath() {
        return configPath;
    }

    public void setLoadedFrom(LoadedFrom loadedFrom) {
        this.loadedFrom = loadedFrom;
    }

    private InputStream loadDefaultConfig() {
        InputStream is = MainApp.class.getResourceAsStream("/config.json");
        if(is == null){
            logger.error("Failed to load default configuration: inputStream is null");
            NotificationService.show("conf.load.error","Failed to load default configuration",false, BaseException.Type.ERROR);
        }else {
            String path_raw = MainApp.class.getResource("/config.json").getPath();
            path_raw = path_raw.startsWith("/") ? path_raw.substring(1) : path_raw;
            configPath = PathManager.normalize(path_raw);
        }
        return is;
    }
}
