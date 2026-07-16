package org.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.launcher.async.AdminSessionControlAsync;
import org.launcher.async.UiTimer;
import org.launcher.config.ConfigurationControl;
import org.launcher.config.Localization;
import org.launcher.controller.KeyboardController;
import org.launcher.controller.ui.AdminController;
import org.launcher.controller.ui.MainController;
import org.launcher.controller.MainWindowController;
import org.launcher.controller.ui.WaitController;
import org.launcher.service.NotificationService;
import org.launcher.utils.PathManager;
import org.launcher.utils.WatchdogClient;
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
    private MainController mainController = null;
    private WaitController waitController =null;
    private AdminController adminController = null;
    private MainWindowController mainWindowController;
    private KeyboardController keyboardController;
    private Thread keyloggerThread = null;
    private Stage mainStage;
    private Scene mainScene;
    private volatile String rootId;

    @Override
    public void init() throws Exception {
        NotificationService.show("app.startup", "Starting launcher...",false, 2L, null);
        loadConfiguration();
        mainWindowController = new MainWindowController();
        AdminSessionControlAsync.initialize(configurationControl,this);
        if (configurationControl.isLoaded()) {
            keyboardController = new KeyboardController(configurationControl.getConfiguration().getAdmin().getCombination(), this);
        }
        WatchdogClient.start();
        logger = LoggerFactory.getLogger(MainApp.class);
        Localization.load();
        logger.info("Launcher started");
        super.init();
    }

    @Override
    public void stop() throws Exception {
        try {
            stopAllControllers();
            if (keyboardController != null) {
                keyboardController.stop();
                keyboardController = null;
            }
            AdminSessionControlAsync.stop();
            if (keyloggerThread != null) {
                keyloggerThread.interrupt();
            }
            UiTimer.stop();
            WatchdogClient.stop();
        } finally {
            NotificationService.stopExecutor();
            logger.info("Launcher stopped");
            super.stop();
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Launcher");
        preventExit(stage);
        mainStage = stage;
        reloadScene(stage, null);
    }

    public void reloadScene(Stage stage, String viewType) {
        try {
            stopAllControllers();
            if (viewType == null) {
                viewType = configurationControl.isLoaded() ? "main" : "wait";
            }
            FXMLLoader fxmlLoader;
            if (!configurationControl.isLoaded() && !viewType.equals("admin")) {
                fxmlLoader = new FXMLLoader(MainApp.class.getResource("wait-view.fxml"));
            } else {
                fxmlLoader = viewType.equals("admin") ?
                        new FXMLLoader(MainApp.class.getResource("admin-view.fxml")) :
                        new FXMLLoader(MainApp.class.getResource("main-view.fxml"));
            }
            makeUiControllers(fxmlLoader, viewType);
            Parent root = fxmlLoader.load();
            root.setId(viewType);
            if (mainScene == null) {
                mainScene = new Scene(root);
            } else {
                mainStage.getScene().setRoot(root);
            }
            String css = Objects.requireNonNull(MainApp.class.getResource("css/main.css")).toExternalForm();
            if (!mainScene.getStylesheets().contains(css)) {
                mainScene.getStylesheets().add(css);
            }
            if (stage.getScene() == null) {
                stage.setScene(mainScene);
                stage.show();
            }
            switch (viewType) {
                case "main" -> {
                    Platform.runLater(() -> {
                        long hwnd = findWindow(stage);
                        mainWindowController.setHwnd(hwnd);
                        applyNoActivate(hwnd);
                    });
                    if(keyloggerThread == null) {
                        keyloggerThread = new Thread(() -> keyboardController.start());
                        keyloggerThread.setDaemon(true);
                        keyloggerThread.setName("KeyloggerThread");
                        keyloggerThread.start();
                    }
                    mainController = fxmlLoader.getController();
                    mainController.initWindowTracking();
                    Runnable loadSceneListener = () -> mainController.calculateAppListSize();
                    Platform.runLater(loadSceneListener);
                }
                case "wait" -> waitController = fxmlLoader.getController();
                case "admin" -> adminController = fxmlLoader.getController();
            }
            rootId = viewType;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadScene(String viewType) {
        reloadScene(mainStage, viewType);
    }

    public String getRootId() {
        return rootId;
    }

    public static void main(String[] args) {
       // System.out.println(KeyboardEventConstants.INTKEYS_STRKEYS.get(39));
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

    public MainController getMainController() {
        return mainController;
    }

    public WaitController getWaitController() {
        return waitController;
    }

    public AdminController getAdminController() {
        return adminController;
    }

    private void makeUiControllers(FXMLLoader fxmlLoader, String type) {
        fxmlLoader.setControllerFactory(param ->
                switch (type) {
                    case "main" -> new MainController(configurationControl);
                    case "wait" -> new WaitController(configurationControl.getConfiguration());
                    case "admin" -> new AdminController(configurationControl);
                    default -> throw new IllegalArgumentException("Unknown controller type: " + type);
                }
        );
    }

    private void preventExit(Stage stage) {
        stage.setFullScreen(false);
        stage.setMaximized(true);
        stage.setOnCloseRequest(Event::consume);
        stage.setAlwaysOnTop(false);
        stage.toFront();
    }

    private void stopAllControllers() {
        if (mainController != null) {
            mainController.stopAll();
        }
        mainController = null;
        waitController = null;
        adminController = null;
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
        boolean fromParameter = true;
        Parameters parameters = getParameters();
        Map<String, String> named = parameters.getNamed();
        String pathToConfig = named.getOrDefault("config", null);
        if (pathToConfig == null) {
            fromParameter = false;
            Path nearConfig = PathManager.getAppDir().resolve("config.json");
            pathToConfig = Files.exists(nearConfig) ? nearConfig.toAbsolutePath().toString() : null;
        }
        configurationControl = new ConfigurationControl(pathToConfig);
        if (fromParameter) {
            configurationControl.setLoadedFrom(ConfigurationControl.LoadedFrom.PARAMETER);
        }
        if (pathToConfig != null) {
            configurationControl.setLoadedFrom(ConfigurationControl.LoadedFrom.APP_DIRECTORY);
        }
    }
}