package org.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.launcher.config.ConfigurationControl;
import org.launcher.config.Localization;
import org.launcher.controller.KeyboardController;
import org.launcher.controller.ui.MainController;
import org.launcher.controller.MainWindowController;
import org.launcher.controller.ui.WaitController;
import org.launcher.service.NotificationService;
import org.launcher.utils.PathManager;
import org.launcher.utils.jnr.lib.User32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class MainApp extends Application {
    private static Logger logger;
    private ConfigurationControl configurationControl;
    private MainController mainController;
    private WaitController waitController;
    private MainWindowController mainWindowController;
    private KeyboardController keyboardController;
    private Thread keyloggerThread;

    @Override
    public void init() throws Exception {
        NotificationService.show("app.startup","Starting launcher...",2L, null);
        loadConfiguration();
        mainWindowController = new MainWindowController();
        if(configurationControl.isLoaded()){
            keyboardController = new KeyboardController(configurationControl.getConfiguration().getAdmin().getCombination());
        }
        logger = LoggerFactory.getLogger(MainApp.class);
        Localization.load();
        logger.info("Launcher started");
        super.init();
    }

    @Override
    public void stop() throws Exception {
        stopAllControllers();
        if(keyloggerThread != null) {
            keyloggerThread.interrupt();
        }
        super.stop();
        NotificationService.stopExecutor();
        logger.info("Launcher stopped");
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Launcher");
        reloadScene(stage,null);
    }

    public void reloadScene(Stage stage,String viewType){
        try {
            stopAllControllers();

            if(viewType == null) {
                viewType = configurationControl.isLoaded() ? "main" : "wait";
            }
            FXMLLoader fxmlLoader = configurationControl.isLoaded() ?
                    new FXMLLoader(MainApp.class.getResource("main-view.fxml")) :
                    new FXMLLoader(MainApp.class.getResource("wait-view.fxml"));
            makeUiControllers(fxmlLoader,viewType);
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(Objects.requireNonNull(MainApp.class.getResource("css/main.css")).toExternalForm());
            stage.setScene(scene);
            preventExit(stage);
            stage.show();
            Platform.runLater(() ->{
                //Pointer hwnd_ptr = User32.INSTANCE.GetForegroundWindow();
                long hwnd = findWindow(stage);//hwnd_ptr.address();//getHwnd();
                mainWindowController.setHwnd(hwnd);
                applyNoActivate(hwnd);
            });
            if(configurationControl.isLoaded()) {
                keyloggerThread = new Thread(() -> keyboardController.start());
                keyloggerThread.setDaemon(true);
                keyloggerThread.setName("KeyloggerThread");
                keyloggerThread.start();
                mainController = fxmlLoader.getController();
                mainController.initWindowTracking();
                Runnable loadSceneListener = () -> mainController.calculateAppListSize();
                Platform.runLater(loadSceneListener);
            }else{
                waitController = new WaitController(configurationControl.getConfiguration());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void applyNoActivate(long hwnd) {
        int GWL_EXSTYLE = -20;
        int WS_EX_NOACTIVATE = 0x08000000;

        long exStyle = User32.INSTANCE.GetWindowLongPtrA(hwnd, GWL_EXSTYLE);
        exStyle |= WS_EX_NOACTIVATE;
        User32.INSTANCE.SetWindowLongPtrA(hwnd, GWL_EXSTYLE, exStyle);
        User32.INSTANCE.SetWindowPos(
                hwnd,
                0,
                0, 0, 0, 0,
                0x0020 | 0x0001 | 0x0002
        );
    }

    private void makeUiControllers(FXMLLoader fxmlLoader, String type) {
        fxmlLoader.setControllerFactory(param ->
                switch (type) {
                  case "main" ->  new MainController(configurationControl);
                  case "wait" -> new WaitController(configurationControl.getConfiguration());
                  default -> throw new IllegalArgumentException("Unknown controller type: " + type);
                }
        );
    }

    private void preventExit(Stage stage) {
        stage.setFullScreen(false);
        stage.setMaximized(true);
        stage.setOnCloseRequest(Event::consume);
        stage.setAlwaysOnTop(false);
        stage.toBack();
    }

    private void stopAllControllers(){
        if(mainController != null) {
            mainController.stopAll();
            mainController = null;
        }
        if(waitController != null) {
            waitController = null;
        }
        if(keyboardController != null) {
            keyboardController.stop();
        }
    }

    private long findWindow(Stage stage) {
        long pid = ProcessHandle.current().pid();
        String title = stage.getTitle();

        AtomicLong result = new AtomicLong();

        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            int[] p = new int[1];
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, p);

            if (p[0] != pid) {
                return 1;
            }

            char[] buf = new char[512];
            int len = User32.INSTANCE.GetWindowTextW(hWnd, buf, buf.length);
            String windowTitle = new String(buf, 0, len);

            if (title.equals(windowTitle)) {
                result.set(hWnd.address());
                return 0;
            }

            return 1;
        }, null);

        return result.get();
    }

    private void loadConfiguration() {
        Parameters parameters = getParameters();
        Map<String, String> named = parameters.getNamed();
        String pathToConfig = named.getOrDefault("config", null);
        if (pathToConfig == null) {
            Path nearConfig = PathManager.getAppDir().resolve("config.json");
            pathToConfig = Files.exists(nearConfig) ? nearConfig.toAbsolutePath().toString() : null;
        }
        configurationControl = new ConfigurationControl(pathToConfig);
    }
}