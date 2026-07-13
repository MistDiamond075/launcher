package org.launcher.controller.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.launcher.async.AdminSessionControlAsync;
import org.launcher.config.ConfigurationControl;
import org.launcher.exception.BaseException;
import org.launcher.service.AdminService;
import org.launcher.service.NotificationService;
import org.launcher.utils.PasswordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;
    @FXML
    private Label systemMessage;
    @FXML
    private HBox systemMessageContainer;
    @FXML
    private StackPane rootStackPane;
    @FXML
    private TextField passwordInput;
    @FXML
    private TextField newPasswordInput;
    @FXML
    private HBox passwordScreen;
    @FXML
    private FlowPane buttonsContainer;
    @FXML
    private Label configStatus;
    private final ConfigurationControl configurationControl;

    public AdminController(ConfigurationControl configurationControl) {
        this.configurationControl = configurationControl;
        this.adminService = new AdminService(configurationControl);
    }

    @FXML
    public void initialize() {
        NotificationService.initialize(systemMessageContainer, systemMessage);
        PasswordManager.inititalize(configurationControl);
        newPasswordInput.setVisible(false);
        newPasswordInput.setManaged(false);
        passwordInput.addEventFilter(KeyEvent.ANY, Event::consume);
        newPasswordInput.addEventFilter(KeyEvent.ANY, Event::consume);
        systemMessage.maxWidthProperty().bind(
                systemMessageContainer.widthProperty().multiply(0.8)
        );
        systemMessageContainer.maxWidthProperty().bind(
                rootStackPane.widthProperty().multiply(0.8)
        );
        systemMessageContainer.setPrefWidth(100);
        setConfigStatusText();
        addControlButtons();
        authorizeListener();
    }

    public HBox getPasswordScreen() {
        return passwordScreen;
    }

    public void appendInput(String input){
        Platform.runLater(() -> {
            TextField field = newPasswordInput.isVisible() ? newPasswordInput : passwordInput;
            switch (input) {
                case "ENTER" -> field.fireEvent(new ActionEvent(field,field));
                case "BACKSPACE" -> field.deletePreviousChar();
                case "DELETE" -> field.deleteNextChar();
                case "SPACE" -> field.appendText(" ");
                case "R_ARROW" -> field.forward();
                case "L_ARROW" -> field.backward();
                default -> field.appendText(input);
            }
        });
    }

    private void authorizeListener(){
        passwordInput.setOnAction(e -> {
            if(!PasswordManager.isPasswordValid(passwordInput.getText())){
                logger.warn("Log in attempt failed: invalid password");
            }else{
                logger.info("Logged successfully");
                AdminSessionControlAsync.start();
                if(!PasswordManager.isPasswordHash()){
                    passwordInput.setVisible(false);
                    newPasswordInput.setVisible(true);
                    passwordInput.setManaged(false);
                    newPasswordInput.setManaged(true);
                }else {
                    passwordScreen.setVisible(false);
                }
                passwordInput.clear();
            }
        });
        newPasswordInput.setOnAction(e -> {
            PasswordManager.setPassword(newPasswordInput.getText());
            newPasswordInput.setManaged(false);
            newPasswordInput.setVisible(false);
            passwordInput.setVisible(true);
            passwordInput.setManaged(true);
            passwordScreen.setVisible(false);
            newPasswordInput.clear();
        });
    }

    private void addControlButtons() {
        Button reloadButton = createButton(
                "Редактировать конфиг",
                e -> {
                    NotificationService.show("conf.load.processing","Loading config...",false, BaseException.Type.INFO);
                    Process editor = adminService.openTextEditor(String.valueOf(configurationControl.getConfigPath()));
                    editor.onExit().thenRun(configurationControl::reload);
                }
        );
        Button openLogsButton = createButton(
                "Посмотреть логи",
                e ->{
                    NotificationService.show("admin.logs.open","Loading logs...",false, BaseException.Type.INFO);
                    adminService.openTextEditor(configurationControl.getConfiguration().getLog().getPath());
                }
        );

        Button exportLogsButton = createButton(
                "Экспорт логов",
                e -> {
                    NotificationService.show("admin.logs.export","Exporting logs...",false, BaseException.Type.INFO);
                    adminService.exportLogs();
                }
        );

        Button shutdownButton = createButton(
                "Выключить",
                e ->{
                    NotificationService.show("app.shutdown","Shutting down",false, BaseException.Type.INFO);
                    PauseTransition pause = new PauseTransition(Duration.seconds(1));
                    pause.setOnFinished(event -> Platform.exit());
                    pause.play();
                });

        Button restartButton = createButton(
                "Перезапустить",
                e ->{
                    NotificationService.show("app.reboot","Restarting",false, BaseException.Type.INFO);
                    adminService.reboot();
                }
        );

        Button explorerButton = createButton("Explorer", e -> adminService.startExplorer());

        buttonsContainer.getChildren().add(reloadButton);
        buttonsContainer.getChildren().add(openLogsButton);
        buttonsContainer.getChildren().add(exportLogsButton);
        buttonsContainer.getChildren().add(explorerButton);
        buttonsContainer.getChildren().add(shutdownButton);
        buttonsContainer.getChildren().add(restartButton);
    }

    private void setConfigStatusText(){
        switch (configurationControl.getLoadedFrom()){
            case PARAMETER -> configStatus.setText("Configuration status: Loaded from parameter");
            case APP_DIRECTORY -> configStatus.setText("Configuration status: Loaded nearest config from app directory");
            case DEFAULT -> configStatus.setText("Configuration status: Loaded default config");
            case FAIL -> configStatus.setText("Configuration status: FAILED");
            default -> configStatus.setText("Configuration status: Source unknown, but OK");
        }
    }

    private Button createButton(String text, EventHandler<javafx.event.ActionEvent> handler){
        Button button = new Button();
        Label t = new Label(text);
        t.getStyleClass().add("app-container-button-label");
        t.setTextAlignment(TextAlignment.CENTER);
        t.setWrapText(true);
        button.setGraphic(t);
        button.setAlignment(Pos.CENTER);
        button.getStyleClass().add("app-container-button");
        button.setOnAction(handler);
//        t.maxWidthProperty().bind(button.widthProperty().subtract(20));

        return button;
    }
}
