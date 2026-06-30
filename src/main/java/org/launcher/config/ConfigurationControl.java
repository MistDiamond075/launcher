package org.launcher.config;

import ch.qos.logback.classic.Level;
import org.launcher.entity.ConfigurationEntity;
import org.launcher.entity.LoggingEntity;
import org.launcher.exception.BaseException;
import org.launcher.exception.EntityValidationException;
import org.launcher.service.NotificationService;
import org.launcher.utils.logging.AppLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigurationControl {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationControl.class);
    private final Path defaultConfig = Path.of("src/main/resources/defaults.properties");
    private final ObjectMapperConfiguration objectMapper = new ObjectMapperConfiguration();
    private final Path configPath;
    private final Properties properties = new Properties();
    private ConfigurationEntity configuration;
    private AppLogger appLogger = null;
    private boolean loaded = false;

    public ConfigurationControl(String configFile) {
        Path tempPath=null;
        try{
            tempPath = Paths.get(configFile);
        } catch (InvalidPathException | NullPointerException e) {
            tempPath = fallback();
            logger.warn("Non-specified or invalid configuration file {}. Loaded default configuration instead", configFile);
        }finally {
            configPath = tempPath;
        }
        boolean exists = configPath != null && Files.exists(configPath);
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
            configuration = objectMapper.getMapper().readValue(configPath.toFile(), ConfigurationEntity.class);
            loaded = true;
            if(appLogger != null){
                appLogger.reload(configuration);
            }else{
                appLogger = new AppLogger(configuration);
            }
            logger.info("Configuration reloaded");
            NotificationService.show("conf.load.success","Configuration reloaded", BaseException.Type.INFO);
        } catch (JacksonException e) {
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

    private Path fallback(){
        try (Reader reader = Files.newBufferedReader(
                defaultConfig,
                StandardCharsets.UTF_8
        )) {
            properties.load(reader);
            return Path.of(properties.getProperty("path.config"));
        } catch (IOException e) {
            logger.error("Failed to load default configuration: {}", e.getMessage());
            NotificationService.show("conf.load.error","Failed to load configuration", BaseException.Type.ERROR);
            logger.debug("Details: ", e);
            return null;
        }
    }
}
