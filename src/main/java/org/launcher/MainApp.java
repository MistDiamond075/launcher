package org.launcher;

import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.launcher.config.ConfigurationControl;
import org.launcher.controller.MainController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private ConfigurationControl configurationControl;
    private MainController mainController;

    @Override
    public void init() throws Exception {
        logger.info("Launcher started");
        configurationControl = new ConfigurationControl(null);
        super.init();
    }

    @Override
    public void stop() throws Exception {
        if(mainController != null) {
            mainController.stopAll();
        }
        super.stop();
        logger.info("Launcher stopped");
    }

    @Override
    public void start(Stage stage) throws IOException {
        KeyCombination exitCombination =
                new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN);

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("main-view.fxml"));
        makeControllers(fxmlLoader);
        Scene scene = new Scene(fxmlLoader.load(), 1280, 900);
        scene.getStylesheets().add(Objects.requireNonNull(MainApp.class.getResource("css/main.css")).toExternalForm());
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        scene.setOnKeyPressed(event -> {
            logger.info("Pressed: {}", event.getCode());
            if (exitCombination.match(event)) {
                stage.close();
            }
        });
        //preventExit(stage);
        stage.show();
        mainController = fxmlLoader.getController();
        mainController.initWindowTracking();
    }
    //"C:\\Users\\egoru\\Downloads\\_lKhEEL0B88.jpg",
    public static void main(String[] args) {
        launch();
    }

    private void makeControllers(FXMLLoader fxmlLoader) {
        fxmlLoader.setControllerFactory(param ->
                new MainController(configurationControl)
        );
    }

    private void preventExit(Stage stage) {
        stage.setFullScreen(true);
        stage.setOnCloseRequest(Event::consume);
        //stage.setAlwaysOnTop(true);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }
}