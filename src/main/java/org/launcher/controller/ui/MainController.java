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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import org.launcher.MainApp;
import org.launcher.async.SessionControlAsync;
import org.launcher.async.UiTimer;
import org.launcher.config.ConfigurationControl;
import org.launcher.config.Localization;
import org.launcher.entity.FolderEntity;
import org.launcher.entity.InstanceEntity;
import org.launcher.exception.BaseException;
import org.launcher.service.AppService;
import org.launcher.entity.AppEntity;
import org.launcher.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
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
    private final Map<FolderEntity,List<Node>> folder_buttons = new HashMap<>();
    @FXML
    private VBox folderContainer;
    @FXML
    private Label folderContainerHeader;
    @FXML
    private FlowPane folderAppsContainer;
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
    @FXML
    private Button startSessionButton;
    @FXML
    private Button stopSessionButton;
    @FXML
    private HBox globalPlaceholder;

    public MainController(ConfigurationControl configurationControl) {
        this.configurationControl = configurationControl;
        appService = new AppService(configurationControl);
    }

    @FXML
    public void initialize() {
        setHeader();
        setAppList();
        subscribeToInstances();
        String bgPath = MainApp.class.getResource("/bg.jpg").toExternalForm();
        rootStackPane.setStyle("-fx-background-image: url('"+ bgPath+"');");
        globalPlaceholder.setStyle("-fx-background-image: url('"+ bgPath+"');");
        NotificationService.initialize(systemMessageContainer, systemMessage);
        systemMessage.maxWidthProperty().bind(
                systemMessageContainer.widthProperty().multiply(0.8)
        );
        systemMessageContainer.maxWidthProperty().bind(
                rootStackPane.widthProperty().multiply(0.8)
        );
        systemMessageContainer.setPrefWidth(100);
        folderContainer.prefWidthProperty().bind(
                rootStackPane.widthProperty().multiply(0.7)
        );
        folderContainer.prefHeightProperty().bind(
                rootStackPane.heightProperty().multiply(0.7)
        );
        folderContainer.maxWidthProperty().bind(folderContainer.prefWidthProperty());
        folderContainer.maxHeightProperty().bind(folderContainer.prefHeightProperty());
        folderAppsContainer.prefWidthProperty().bind(
                folderContainer.widthProperty().multiply(0.9)
        );
        folderAppsContainer.prefHeightProperty().bind(
                folderContainer.heightProperty().multiply(0.9)
        );
        folderContainer.setVisible(false);
        rootStackPane.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->{
            Node target = (Node) e.getTarget();
            if(target != null && !folderContainer.isHover()) {
                folderContainer.setVisible(false);
            }
        });
        appsContainer.widthProperty().addListener((obs, oldVal, newVal) -> calculateAppListSize());
        globalPlaceholder.setVisible(true);
        startSessionButton.setOnAction(e -> startSession());
        stopSessionButton.setOnAction(e -> stopSession());
        timer.getStyleClass().add("timer");
        UiTimer.start(timer);

        logger.debug("MainController initialized");
    }

    public void stopAll(){
        //appService.shutdownWindowEvent();
    }

    public void startSession(){
        SessionControlAsync.start(SessionControlAsync.SessionType.USER);
        globalPlaceholder.setVisible(false);
    }

    public void stopSession(){
        SessionControlAsync.cancel();
        globalPlaceholder.setVisible(true);
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
        Set<AppEntity> usedApps = addFoldersToAppList();
        Set<AppEntity> apps = configurationControl.getConfiguration().getApps();
        apps.removeAll(usedApps);
        for(AppEntity app : apps){
            String ico = app.getIcon() != null ? app.getIcon().toUri().toString() : null;
            Node appButton = addAppListButton(app.getName(),ico,app.isEnableInstancesCounter(),app.getId(),app.isEnabled(),appsContainer);
           setAppStartButtonAction(appButton,app);
            app_button_all.put(app.getId(), appButton);
            logger.info("Loaded app {}", app.getId());
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

    private Set<AppEntity> addFoldersToAppList(){
        Set<AppEntity> apps = new HashSet<>();
        int counter = 1;
        for(FolderEntity f : configurationControl.getConfiguration().getFolders()){
            URL iconResource = MainApp.class.getResource("/folder-icon.png");
            String ico = iconResource != null ? iconResource.toExternalForm() : null;
            Button folderIcon = (Button) addAppListButton(f.getName(), ico, false, "folder"+counter, true, appsContainer);
            List<Node> btns = new ArrayList<>();
            apps.addAll(f.getApps());
            for(AppEntity app : f.getApps()){
                Node btn = addAppListButton(app.getName(),app.getIcon().toUri().toString(),app.isEnableInstancesCounter(),app.getId(),app.isEnabled(), folderAppsContainer);
                btns.add(btn);
                app_button_all.put(app.getId(), btn);
                setAppStartButtonAction(btn,app);
            }
            folderIcon.setOnAction(e ->{
                fillFolderContainer(f);
                folderContainer.setVisible(true);
            });
            folder_buttons.put(f, btns);
            counter++;
        }
        return apps;
    }

    private void fillFolderContainer(FolderEntity folder){
        List<Node> buttons = folder_buttons.getOrDefault(folder, new ArrayList<>());
        folderAppsContainer.getChildren().clear();
        for(Node button : buttons){
            folderAppsContainer.getChildren().add(button);
        }
        folderContainerHeader.setText(folder.getName());
    }

    private void setAppStartButtonAction(Node appButton, AppEntity app){
        if(appButton instanceof Button) {
            ((Button)appButton).setOnAction(e -> {
                if (!appButton.getStyleClass().contains("app-container-button-blocked") && !appButton.getStyleClass().contains("app-container-button-disabled")) {
                    start(app);
                }
            });
        }else{
            ((Button)((StackPane)appButton).getChildren().getFirst()).setOnAction(e -> {
                if (!appButton.getStyleClass().contains("app-container-button-blocked") && !appButton.getStyleClass().contains("app-container-button-disabled")) {
                    start(app);
                }
            });
        }
    }

    private Node addAppListButton(String name,String icon,boolean addInstanceCounter,String id, boolean enabled, FlowPane container){
        Node result;
        Button appButton = new Button();
        Label t = new Label(name);
        t.getStyleClass().add("app-container-button-text");
        t.setWrapText(false);
        appButton.setGraphic(t);
        appButton.setAlignment(Pos.BOTTOM_CENTER);
        appButton.getStyleClass().add("app-container-button");
        if(icon!=null){
            logger.debug("{} icon path={}",name, icon);
            appButton.setStyle(
                    "-fx-background-image: url('"+ icon+"');" +
                            "-fx-background-repeat: no-repeat; " +
                            "-fx-background-position: center;" +
                            "-fx-background-size: 100px"
            );

        }

        if(addInstanceCounter) {
            IntegerProperty counterValue = new SimpleIntegerProperty(0);
            appCounters.put(id, counterValue);

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
            container.getChildren().add(fullButton);
            if(!enabled){
                fullButton.getStyleClass().add("app-container-button-disabled");
                fullButton.setMouseTransparent(true);
            }
            result = fullButton;
        }else{
            result = appButton;
            container.getChildren().add(appButton);
        }
        if(!enabled){
            appButton.getStyleClass().add("app-container-button-disabled");
            appButton.setMouseTransparent(true);
        }
        return result;
    }

    private void start(AppEntity app) {
        appService.start(app);
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
            //counter.set(instance.getApp().getHwnds().size());
        }

        attachStateListener(instance);
        updateButtonState(instance);
    }

    private void onInstanceRemoved(InstanceEntity instance) {
        String appId = instance.getApp().getId();

        IntegerProperty counter = appCounters.get(appId);
        if (counter != null) {
          //  counter.set(instance.getApp().getHwnds().size());
        }

        detachStateListener(instance);
    }

    private void attachStateListener(InstanceEntity instance) {
        ChangeListener<InstanceEntity.State> listener = (obs, oldState, newState) ->
                Platform.runLater(() -> {
                    if(newState == InstanceEntity.State.CLOSED){ //&& instance.getHwnds().isEmpty()){
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