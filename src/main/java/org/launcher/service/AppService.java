package org.launcher.service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.launcher.config.ConfigurationControl;
import org.launcher.entity.AppEntity;
import org.launcher.entity.InstanceEntity;
import org.launcher.events.ForeignWindowEvent;
import org.launcher.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AppService {
    private static final Logger logger = LoggerFactory.getLogger(AppService.class);
    private final Map<AppEntity, Set<InstanceEntity>> started = new ConcurrentHashMap<>();
    private final ObservableList<InstanceEntity> instances = FXCollections.observableArrayList();
    private ForeignWindowEvent foreignWindowEvent;
    private final ConfigurationControl configurationControl;
    //sample text sample text sample text
    public AppService(ConfigurationControl configurationControl) {
        this.configurationControl = configurationControl;
    }

    public void start(AppEntity entity) {
        ProcessBuilder processBuilder = createProcess(entity);
        Process process;
        try {
            if(processBuilder == null) {
                throw new NullPointerException("processBuilder is null");
            }
            if(started.containsKey(entity) && (!entity.isAllowMultipleInstances() || !configurationControl.getConfiguration().getLauncher().isAllowMultipleInstances())) {
                String log_message = !configurationControl.getConfiguration().getLauncher().isAllowMultipleInstances() ?
                        "Current config doesn't allow multiple instances" :
                        "Application {} allows only one instance";
                logger.warn(log_message, entity.getId());
                NotificationService.show("service.apps.app.instance_start.not_allowed","Only one instance allowed",false, BaseException.Type.WARNING);
                return;
            }
            process = processBuilder.start();
            InstanceEntity proc = new InstanceEntity(entity, process);
            started.computeIfAbsent(entity, k -> ConcurrentHashMap.newKeySet()).add(proc);
            Platform.runLater(() -> instances.add(proc));
            if(foreignWindowEvent != null) {
                foreignWindowEvent.register(proc);
            }
            logger.info("Started application {}, PID: {}",entity.getId(),process.pid());
            NotificationService.show("service.apps.app.start.success","App started",false, BaseException.Type.INFO);
        } catch (NullPointerException | IOException e) {
            logger.error("Failed to start process: {}",e.getMessage());
            logger.debug("Details: ", e);
            NotificationService.show("service.apps.app.start.fail","Failed to start app",false,BaseException.Type.ERROR);
        }
    }

    public ObservableList<InstanceEntity> getInstances() {
        return instances;
    }

    public void removeInstance(InstanceEntity instance) {
        Platform.runLater(() -> {
            instance.getApp().getHwnds().removeAll(instance.getHwnds());
            instances.remove(instance);
            Set<InstanceEntity> instanceSet = started.get(instance.getApp());
            if(instanceSet != null) {
                instanceSet.remove(instance);
                if(instanceSet.isEmpty()) {
                    started.remove(instance.getApp());
                }
            }
        });
    }

    public void initWindowEvent() {
        if(foreignWindowEvent == null) {
            foreignWindowEvent = new ForeignWindowEvent();
            foreignWindowEvent.start();
        }
    }

    public void shutdownWindowEvent() {
        foreignWindowEvent.close();
        for(InstanceEntity instance : instances) {
            instance.getProcess().destroy();
        }
    }

    private ProcessBuilder createProcess(AppEntity entity) {
        File workingDir;
        try {
            workingDir = entity.getWorkingDirectory().toFile();
        } catch (UnsupportedOperationException e) {
           logger.error("Failed to get working directory: {}", e.getMessage());
           logger.debug("Details", e);
           return null;
        }
        List<String> cmd = new ArrayList<>();
        cmd.add(entity.getPath().toString());
        List<String> args = Arrays.asList(entity.getArguments().split("\\s+"));
        if(!entity.getArguments().isEmpty()) {
            cmd.addAll(args);
        }
        return new ProcessBuilder()
                .directory(workingDir)
                .command(cmd);
    }
}
