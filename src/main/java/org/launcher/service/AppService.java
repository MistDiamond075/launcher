package org.launcher.service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.launcher.entity.AppEntity;
import org.launcher.entity.InstanceEntity;
import org.launcher.events.ForeignWindowEvent;
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

    public AppService() {}

    public void start(AppEntity entity) {
        ProcessBuilder processBuilder = createProcess(entity);
        Process process;
        try {
            if(processBuilder == null) {
                throw new NullPointerException("processBuilder is null");
            }
            if(started.containsKey(entity) && !entity.isAllowMultipleInstances()) {
                logger.warn("Application {} allows only one instance", entity.getId());
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
        } catch (NullPointerException | IOException e) {
            logger.error("Failed to start process: {}",e.getMessage());
            logger.debug("Details: ", e);
        }
    }

    public ObservableList<InstanceEntity> getInstances() {
        return instances;
    }

    public void removeInstance(InstanceEntity instance) {
        Platform.runLater(() -> {
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
