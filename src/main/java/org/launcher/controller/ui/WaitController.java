package org.launcher.controller.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.launcher.entity.ConfigurationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitController {
    private static final Logger logger = LoggerFactory.getLogger(WaitController.class);
    private ConfigurationEntity configuration;
    @FXML
    private Label labelHeader;

    public WaitController(ConfigurationEntity configuration) {
        this.configuration = configuration;
    }

    @FXML
    public void initialize() {
        String title = configuration != null ? configuration.getLauncher().getTitle() : "undefined title";
        labelHeader.setText(title);
    }
}
