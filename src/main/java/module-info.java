module org.launcher {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires java.logging;
    requires java.desktop;
    requires org.jnrproject.ffi;
    requires de.mkammerer.argon2.nolibs;
    exports org.launcher;

    opens org.launcher.utils to ch.qos.logback.core;
    opens org.launcher;
    exports org.launcher.controller;
    exports org.launcher.utils.jnr.struct;
    opens org.launcher.controller to javafx.fxml, tools.jackson.databind;
    exports org.launcher.entity.properties;
    exports org.launcher.config;
    exports org.launcher.entity;
    exports org.launcher.exception;
    exports org.launcher.service;
    opens org.launcher.utils.logging to ch.qos.logback.core;
    exports org.launcher.utils.jnr.lib;
    exports org.launcher.utils.jnr.callback;
    exports org.launcher.controller.ui;
    opens org.launcher.controller.ui to javafx.fxml, tools.jackson.databind;
    opens org.launcher.utils.constants to ch.qos.logback.core;
    exports org.launcher.utils;
    exports org.launcher.utils.icons;
    opens org.launcher.utils.icons to ch.qos.logback.core;
}