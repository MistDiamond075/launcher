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
import org.launcher.config.Localization;
import org.launcher.controller.MainController;
import org.launcher.controller.WaitController;
import org.launcher.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    private static Logger logger;
    private ConfigurationControl configurationControl;
    private MainController mainController;
    private WaitController waitController;

    @Override
    public void init() throws Exception {
        configurationControl = new ConfigurationControl(null);
        logger = LoggerFactory.getLogger(MainApp.class);
        Localization.load();
        logger.info("Launcher started");
        super.init();
    }

    @Override
    public void stop() throws Exception {
        NotificationService.stopExecutor();
        stopAllControllers();
        super.stop();
        logger.info("Launcher stopped");
    }

    @Override
    public void start(Stage stage) throws IOException {
        reloadScene(stage,null);
    }
    //"C:\\Users\\egoru\\Downloads\\_lKhEEL0B88.jpg",

    public void reloadScene(Stage stage,String viewType){
        try {
            stopAllControllers();
            KeyCombination exitCombination =
                    new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN);

            if(viewType == null) {
                viewType = configurationControl.isLoaded() ? "main" : "wait";
            }
            FXMLLoader fxmlLoader = configurationControl.isLoaded() ?
                    new FXMLLoader(MainApp.class.getResource("main-view.fxml")) :
                    new FXMLLoader(MainApp.class.getResource("wait-view.fxml"));
            makeControllers(fxmlLoader,viewType);
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(Objects.requireNonNull(MainApp.class.getResource("css/main.css")).toExternalForm());
            stage.setScene(scene);
            //stage.setAlwaysOnTop(true);
            scene.setOnKeyPressed(event -> {
                logger.info("Pressed: {}", event.getCode());
                if (exitCombination.match(event)) {
                    stage.close();
                }
            });
            preventExit(stage);
            stage.show();
            if(configurationControl.isLoaded()) {
                mainController = fxmlLoader.getController();
                mainController.initWindowTracking();
            }else{
                waitController = new WaitController(configurationControl.getConfiguration());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private void makeControllers(FXMLLoader fxmlLoader,String type) {
        fxmlLoader.setControllerFactory(param ->
                switch (type) {
                  case "main" ->  new MainController(configurationControl);
                  case "wait" -> new WaitController(configurationControl.getConfiguration());
                  default -> throw new IllegalArgumentException("Unknown control type: " + type);
                }
        );
    }

    private void preventExit(Stage stage) {
        stage.setFullScreen(true);
        stage.setOnCloseRequest(Event::consume);
        stage.setAlwaysOnTop(true);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }

    private void stopAllControllers(){
        if(mainController != null) {
            mainController.stopAll();
            mainController = null;
        }
        if(waitController != null) {
            waitController = null;
        }
    }
}