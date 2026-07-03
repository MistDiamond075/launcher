package org.launcher.controller.ui;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import org.launcher.config.ConfigurationControl;
import org.launcher.config.Localization;
import org.launcher.entity.InstanceEntity;
import org.launcher.exception.BaseException;
import org.launcher.service.AppService;
import org.launcher.entity.AppEntity;
import org.launcher.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {
    public enum SystemMessageLevel {ERROR, WARNING, INFO}
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final ConfigurationControl configurationControl;
    private final AppService appService;
    private final Map<String,Button> app_button_all = new LinkedHashMap<>();
    private Map<String,Button> app_button = new HashMap<>();
    private int maxButtonsPerPage = 0;
    private int currentPage = 0;
    private int pages = 0;
    private final Map<InstanceEntity, ChangeListener<InstanceEntity.State>> stateListeners = new HashMap<>();
    private final Map<String, IntegerProperty> appCounters = new HashMap<>();
    @FXML
    private Label labelHeader;
    @FXML
    private Label appsContainerPlaceholder;
    @FXML
    private FlowPane appsContainer;
    @FXML
    private Label systemMessage;
    @FXML
    private HBox systemMessageContainer;
    @FXML
    private StackPane rootStackPane;
    @FXML
    private Button buttonLeft;
    @FXML
    private Button buttonRight;

    public MainController(ConfigurationControl configurationControl) {
        this.configurationControl = configurationControl;
        appService = new AppService(configurationControl);
    }

    @FXML
    public void initialize() {
        setHeader();
        setAppList();
        subscribeToInstances();
        NotificationService.initialize(systemMessageContainer, systemMessage);
        systemMessage.maxWidthProperty().bind(
                systemMessageContainer.widthProperty().multiply(0.8)
        );
        systemMessageContainer.maxWidthProperty().bind(
                rootStackPane.widthProperty().multiply(0.8)
        );
        systemMessageContainer.setPrefWidth(100);

        logger.debug("MainController initialized");
    }

    public void stopAll(){
        appService.shutdownWindowEvent();
    }

    private void setHeader(){
        labelHeader.setText(configurationControl.getConfiguration().getLauncher().getTitle());
    }

    private void setAppList(){
        Set<AppEntity> apps = configurationControl.getConfiguration().getApps();
        for(AppEntity app : apps){

            Button appButton = new Button(app.getName());
            appButton.getStyleClass().add("app-container-button");
            appButton.setOnAction(e -> {
                if(!appButton.getStyleClass().contains("app-container-button-blocked") && !appButton.getStyleClass().contains("app-container-button-disabled")) {
                    start(app);
                }
            });
            if(app.getIcon()!=null){
                logger.debug(app.getIcon().toString());
                appButton.setStyle(
                        "-fx-background-image: url('"+ app.getIcon().toUri()+"');" +
                        "-fx-background-repeat: no-repeat; " +
                        "-fx-background-position: center;" +
                        "-fx-text-fill: transparent;"
                );

            }

            if(app.InstancesCounterEnabled()) {
                IntegerProperty counterValue = new SimpleIntegerProperty(0);
                appCounters.put(app.getId(), counterValue);

                Label instanceCount = new Label();
                instanceCount.getStyleClass().add("app-container-button-counter");
                instanceCount.textProperty().bind(counterValue.asString());
                instanceCount.visibleProperty().bind(counterValue.greaterThan(0));
                instanceCount.managedProperty().bind(instanceCount.visibleProperty());

                StackPane fullButton = new StackPane(appButton);
                StackPane badge = new StackPane(instanceCount);
                badge.setAlignment(Pos.TOP_LEFT);
                badge.setMouseTransparent(true);
                StackPane.setMargin(instanceCount, new Insets(-8, -8, 0, 0));
                fullButton.getChildren().add(badge);
                appsContainer.getChildren().add(fullButton);
            }else{
                appsContainer.getChildren().add(appButton);
            }
            if(!app.isEnabled()){
                appButton.getStyleClass().add("app-container-button-disabled");
            }
            app_button_all.put(app.getId(), appButton);
            logger.debug("Loaded app {}", app.getId());
        }
        logger.info("Loaded {} apps", apps.size());
        NotificationService.show(MessageFormat.format("controller.main.apps.count",apps.size()),"Apps loaded", BaseException.Type.INFO);
        if(!apps.isEmpty()){
            appsContainer.getChildren().remove(appsContainerPlaceholder);
        }else{
            appsContainerPlaceholder.setText(Localization.get("controller.main.apps.empty","No apps specified ¯\\_(ツ)_/¯"));
        }
    }

    private void start(AppEntity app) {
        appService.start(app);
    }

    public void initWindowTracking() {
        appService.initWindowEvent();
    }

    public void calculateAppListSize(){
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double width = visualBounds.getWidth();
        double height = visualBounds.getHeight();
        Optional<Button> btn_opt = app_button_all.values().stream().findFirst();
        if(btn_opt.isEmpty()){
            return;
        }
        Button btn = btn_opt.get();
        double buttonVerticalSize = btn.getHeight() + appsContainer.getVgap();
        double buttonHorizontalSize = btn.getWidth() + appsContainer.getHgap();
        int cols = (int)(width / buttonHorizontalSize);
        int rows = ((int)(height / buttonVerticalSize)) - 1;
        maxButtonsPerPage = cols * rows;
        if(app_button_all.size() <= maxButtonsPerPage){
            buttonLeft.setVisible(false);
            buttonRight.setVisible(false);
            return;
        }
        pages = app_button_all.size() / maxButtonsPerPage;
        switchPageButtonListener(true,buttonRight);
        switchPageButtonListener(false,buttonLeft);
        setAppsOnPage();
    }

    private void switchPageButtonListener(boolean increase,Button button){
        button.setOnAction(e -> {
            if(increase){
                currentPage++;
            }else{
                currentPage--;
            }
            if(currentPage > pages){
                currentPage = 0;
            }else if(currentPage < 0){
                currentPage = pages;
            }
            setAppsOnPage();
        });
    }

    private void subscribeToInstances(){
        appService.getInstances().addListener((ListChangeListener<InstanceEntity>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (InstanceEntity instance : change.getAddedSubList()) {
                        onInstanceAdded(instance);
                    }
                }
                if (change.wasRemoved()) {
                    for (InstanceEntity instance : change.getRemoved()) {
                        onInstanceRemoved(instance);
                    }
                }
            }
        });
    }

    private void onInstanceAdded(InstanceEntity instance) {
        String appId = instance.getApp().getId();

        IntegerProperty counter = appCounters.get(appId);
        if (counter != null) {
            counter.set(counter.get() + 1);
        }

        attachStateListener(instance);
        updateButtonState(instance);
    }

    private void onInstanceRemoved(InstanceEntity instance) {
        String appId = instance.getApp().getId();

        IntegerProperty counter = appCounters.get(appId);
        if (counter != null) {
            counter.set(Math.max(0, counter.get() - 1));
        }

        detachStateListener(instance);
    }

    private void attachStateListener(InstanceEntity instance) {
        ChangeListener<InstanceEntity.State> listener = (obs, oldState, newState) ->
                Platform.runLater(() -> {
                    updateButtonState(instance);
                    if(newState == InstanceEntity.State.CLOSED && instance.getHwnds().isEmpty()){
                        appService.removeInstance(instance);
                    }
                });

        instance.stateProperty().addListener(listener);
        stateListeners.put(instance, listener);
    }

    private void detachStateListener(InstanceEntity instance) {
        ChangeListener<InstanceEntity.State> listener = stateListeners.remove(instance);
        if (listener != null) {
            instance.stateProperty().removeListener(listener);
        }
    }

    private void updateButtonState(InstanceEntity instance) {
        Button button = app_button_all.get( instance.getApp().getId());
        if (button == null) {
            return;
        }
        //logger.info("State: {},pid: {},alive: {}", instance.getState(),instance.getProcess().pid(),instance.getProcess().isAlive());
        switch (instance.getState()) {
            case RUNNING -> {
                if (!button.getStyleClass().contains("app-container-button-active")) {
                    button.getStyleClass().add("app-container-button-active");
                }
            }
            case CLOSED -> {
                button.getStyleClass().remove("app-container-button-active");
                button.getStyleClass().remove("app-container-button-blocked");
            }
            case CLOSING -> {
                button.getStyleClass().remove("app-container-button-active");
                button.getStyleClass().add("app-container-button-closed");
            }
        }
    }

    private void setAppsOnPage(){
        app_button = app_button_all.entrySet()
                .stream()
                .skip((long) currentPage * maxButtonsPerPage)
                .limit(maxButtonsPerPage)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        appsContainer.getChildren().clear();
        appsContainer.getChildren().addAll(app_button.values());
    }
}