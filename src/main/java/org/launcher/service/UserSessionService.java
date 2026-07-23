package org.launcher.service;

import org.launcher.config.ConfigurationControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSessionService {
    private static final Logger logger = LoggerFactory.getLogger(UserSessionService.class);
    private final ConfigurationControl configurationControl;

    public UserSessionService(ConfigurationControl configurationControl) {
        this.configurationControl = configurationControl;
    }
}
