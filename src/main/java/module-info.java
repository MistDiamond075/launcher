module org.launcher {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires java.logging;
    requires java.desktop;
    requires org.jnrproject.ffi;

    exports org.launcher;

    opens org.launcher.utils to ch.qos.logback.core;
    opens org.launcher to javafx.fxml;
    exports org.launcher.controller;
    opens org.launcher.controller to javafx.fxml, tools.jackson.databind;
    exports org.launcher.entity.properties;
    exports org.launcher.config;
    exports org.launcher.entity;
    exports org.launcher.exception;
    exports org.launcher.service;
    exports org.launcher.service.win;
    opens org.launcher.utils.logging to ch.qos.logback.core;
}