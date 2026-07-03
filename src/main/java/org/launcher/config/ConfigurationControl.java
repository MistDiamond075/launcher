package org.launcher.config;

import org.launcher.MainApp;
import org.launcher.entity.ConfigurationEntity;
import org.launcher.exception.BaseException;
import org.launcher.exception.EntityValidationException;
import org.launcher.service.NotificationService;
import org.launcher.utils.logging.AppLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationControl {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationControl.class);
    private final ObjectMapperConfiguration objectMapper = new ObjectMapperConfiguration();
    private boolean useDefaultConfig = false;
    private Path configPath = null;
    private ConfigurationEntity configuration;
    private AppLogger appLogger = null;
    private boolean loaded = false;

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
                configuration = objectMapper.getMapper().readValue(input, ConfigurationEntity.class);
                input.close();
            }else {
                configuration = objectMapper.getMapper().readValue(configPath.toFile(), ConfigurationEntity.class);
            }

            loaded = true;
            if(appLogger != null){
                appLogger.reload(configuration);
            }else{
                appLogger = new AppLogger(configuration);
            }
            logger.info("Configuration reloaded");
            NotificationService.show("conf.load.success","Configuration reloaded", BaseException.Type.INFO);
        } catch (JacksonException|IOException e) {
            logger.error("Failed to parse configuration: {}", e.getMessage());
            logger.debug("Details: ", e);
            NotificationService.show("conf.load.error","Failed to load configuration", BaseException.Type.ERROR);
            loaded = false;
        }
        try{
            configuration.validate();
        }catch(EntityValidationException e){
            logger.error("Configuration invalid: {}", e.getMessage());
            logger.debug("Details: ", e);
            NotificationService.show("conf.load.error","Failed to load configuration", BaseException.Type.ERROR);
            loaded = false;
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    private InputStream loadDefaultConfig() {
        InputStream is = MainApp.class.getResourceAsStream("/config.json");
        if(is == null){
            logger.error("Failed to load default configuration: inputStream is null");
            NotificationService.show("conf.load.error","Failed to load default configuration", BaseException.Type.ERROR);
        }
        return is;
    }
}
