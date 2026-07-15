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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import org.launcher.async.UiTimer;
import org.launcher.config.ConfigurationControl;
import org.launcher.config.Localization;
import org.launcher.entity.InstanceEntity;
import org.launcher.exception.BaseException;
import org.launcher.service.AppService;
import org.launcher.entity.AppEntity;
import org.launcher.service.NotificationService;
import org.launcher.ui.GradientAnimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {
    public enum SystemMessageLevel {ERROR, WARNING, INFO}
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final ConfigurationControl configurationControl;
    private final AppService appService;
    private final Map<String,Node> app_button_all = new LinkedHashMap<>();
    private int maxButtonsPerPage = 0;
    private int currentPage = 0;
    private int pages = 0;
    private final Map<InstanceEntity, ChangeListener<InstanceEntity.State>> stateListeners = new HashMap<>();
    private final Map<String, IntegerProperty> appCounters = new HashMap<>();
    private GradientAnimator gradientAnimator;
    @FXML
    private StackPane headerContainer;
    @FXML
    private Label appsContainerPlaceholder;
    @FXML
    private FlowPane appsContainer;
    @FXML
    private Label systemMessage;
    @FXML
    private HBox systemMessageContainer;
    @FXML
    private Label timer;
    @FXML
    private StackPane rootStackPane;
    @FXML
    private BorderPane appsBorderPane;
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
        if(!configurationControl.getConfiguration().getLauncher().isDisableBackgroundAnimation()) {
            Rectangle2D bounds =
                    Screen.getPrimary().getBounds();

            GradientAnimator background =
                    new GradientAnimator(
                            (int) bounds.getWidth(),
                            (int) bounds.getHeight(),
                            GradientAnimator.rgb(151, 7, 225),
                            GradientAnimator.rgb(12, 3, 71)
                    );
            rootStackPane.getChildren().addFirst(background);
            appsBorderPane.getStyleClass().remove("main-container");
        }
        timer.getStyleClass().add("timer");
        UiTimer.start(timer);

        logger.debug("MainController initialized");
    }

    public void stopAll(){
        appService.shutdownWindowEvent();
    }

    private void setHeader(){
        if(configurationControl.getConfiguration().getLauncher().isTitlePicture()){
            ImageView imageHeader = new ImageView(
                    new Image(Path.of(configurationControl.getConfiguration()
                            .getLauncher()
                            .getTitle()).toUri().toString())
            );
            headerContainer.getStyleClass().add("header-logo");
            imageHeader.setPreserveRatio(true);
            imageHeader.setFitWidth(400);
            imageHeader.setFitHeight(150);
            StackPane.setAlignment(imageHeader, Pos.TOP_CENTER);
            headerContainer.getChildren().add(imageHeader);
        }else {
            Label labelHeader = new Label();
            labelHeader.getStyleClass().add("header");
            labelHeader.setText(configurationControl.getConfiguration().getLauncher().getTitle());
            StackPane.setAlignment(labelHeader,Pos.TOP_CENTER);
            headerContainer.getChildren().add(labelHeader);
        }
    }

    private void setAppList(){
        Set<AppEntity> apps = configurationControl.getConfiguration().getApps();
        for(AppEntity app : apps){

            Button appButton = new Button();
            Label t = new Label(app.getName());
            t.getStyleClass().add("app-container-button-text");
            t.setWrapText(false);
            appButton.setGraphic(t);
            appButton.setAlignment(Pos.BOTTOM_CENTER);
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
                        "-fx-background-size: 100px"
                );

            }

            if(app.isEnableInstancesCounter()) {
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
                app_button_all.put(app.getId(), fullButton);
                if(!app.isEnabled()){
                    fullButton.getStyleClass().add("app-container-button-disabled");
                    fullButton.setMouseTransparent(true);
                }
            }else{
                appsContainer.getChildren().add(appButton);
                app_button_all.put(app.getId(), appButton);
            }
            if(!app.isEnabled()){
                appButton.getStyleClass().add("app-container-button-disabled");
                appButton.setMouseTransparent(true);
            }
            logger.debug("Loaded app {}", app.getId());
        }
        logger.info("Loaded {} apps", apps.size());
        NotificationService.show(
                MessageFormat.format(
                        Localization.get("controller.main.apps.count"),
                        apps.size()
                ),
                "Apps loaded",
                true,
                BaseException.Type.INFO
        );
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
        Optional<Node> btn_opt = app_button_all.values().stream().findFirst();
        if(btn_opt.isEmpty()){
            return;
        }
        Node btn = btn_opt.get();
        double buttonVerticalSize = btn.getLayoutBounds().getHeight() + appsContainer.getVgap();
        double buttonHorizontalSize = btn.getLayoutBounds().getWidth() + appsContainer.getHgap();
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
            counter.set(instance.getApp().getHwnds().size());
        }

        attachStateListener(instance);
        updateButtonState(instance);
    }

    private void onInstanceRemoved(InstanceEntity instance) {
        String appId = instance.getApp().getId();

        IntegerProperty counter = appCounters.get(appId);
        if (counter != null) {
            counter.set(instance.getApp().getHwnds().size());
        }

        detachStateListener(instance);
    }

    private void attachStateListener(InstanceEntity instance) {
        ChangeListener<InstanceEntity.State> listener = (obs, oldState, newState) ->
                Platform.runLater(() -> {
                    if(newState == InstanceEntity.State.CLOSED && instance.getHwnds().isEmpty()){
                        appService.removeInstance(instance);
                    }
                    updateButtonState(instance);
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
        Node button = app_button_all.get(instance.getApp().getId());
        AppEntity app = instance.getApp();
        if (button == null) {
            return;
        }
        switch (instance.getState()) {
            case RUNNING -> {
                if (!button.getStyleClass().contains("app-container-button-active")) {
                    button.getStyleClass().add("app-container-button-active");
                }
            }
            case CLOSED -> {
                if(app.getHwnds().isEmpty()) {
                    button.getStyleClass().remove("app-container-button-active");
                    button.getStyleClass().remove("app-container-button-blocked");
                }
            }
            case CLOSING -> {
                button.getStyleClass().remove("app-container-button-active");
                button.getStyleClass().add("app-container-button-closed");
            }
        }
    }

    private void setAppsOnPage(){
        Map<String, Node> app_button = app_button_all.entrySet()
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