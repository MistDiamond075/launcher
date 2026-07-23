package org.launcher.service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jnr.ffi.Pointer;
import org.launcher.config.ConfigurationControl;
import org.launcher.entity.AppEntity;
import org.launcher.entity.InstanceEntity;
import org.launcher.exception.BaseException;
import org.launcher.utils.jnr.lib.Kernel32;
import org.launcher.utils.jnr.struct.PROCESS_INFORMATION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AppService {
    private static final Logger logger = LoggerFactory.getLogger(AppService.class);
    private final Map<AppEntity, Set<InstanceEntity>> started = new ConcurrentHashMap<>();
    private final ObservableList<InstanceEntity> instances = FXCollections.observableArrayList();
    private final ConfigurationControl configurationControl;
    private final ProcessTrackerService processTrackerService;
    //sample text sample text sample text
    public AppService(ConfigurationControl configurationControl) {
        this.configurationControl = configurationControl;
        processTrackerService = new ProcessTrackerService();
    }

    public void start(AppEntity entity) {
        try {
            if(started.containsKey(entity) && (!entity.isAllowMultipleInstances() || !configurationControl.getConfiguration().getLauncher().isAllowMultipleInstances())) {
                String log_message = !configurationControl.getConfiguration().getLauncher().isAllowMultipleInstances() ?
                        "Current config doesn't allow multiple instances" :
                        "Application {} allows only one instance";
                logger.warn(log_message, entity.getId());
                NotificationService.show("service.apps.app.instance_start.not_allowed","Only one instance allowed",false, BaseException.Type.WARNING);
                return;
            }
            Pointer job = processTrackerService.createJob();
            PROCESS_INFORMATION processInformation = createProcess(entity);
            if (Kernel32.INSTANCE.AssignProcessToJobObject(job, processInformation.hProcess.get()) == 0) {
                Kernel32.INSTANCE.CloseHandle(processInformation.hProcess.get());
                Kernel32.INSTANCE.CloseHandle(job);
                throw new Exception("Failed to assign process to job");
            }
           int result = Kernel32.INSTANCE.ResumeThread(processInformation.hThread.get());
            logger.debug(
                    "ResumeThread returned {}, lastError={}",
                    result,
                    Kernel32.INSTANCE.GetLastError()
            );
            if(result == -1){
                throw new Exception("Failed to resume thread");
            }
            Kernel32.INSTANCE.CloseHandle(processInformation.hThread.get());
            InstanceEntity instance = new InstanceEntity(entity, processInformation, job);
            started.computeIfAbsent(entity, k -> ConcurrentHashMap.newKeySet()).add(instance);
            processTrackerService.registerInstance(instance);
            Platform.runLater(() -> {
                instances.add(instance);
                instance.setState(InstanceEntity.State.RUNNING);
            });
            logger.info("Started application {}, PID: {}",entity.getId(),processInformation);
            NotificationService.show("service.apps.app.start.success","App started",false, BaseException.Type.INFO);
        } catch (Exception e) {
            logger.error("Failed to start process: {}",entity);
            logger.debug("Details: ", e);
            NotificationService.show("service.apps.app.start.fail","Failed to start app",false,BaseException.Type.ERROR);
        }
    }

    public ObservableList<InstanceEntity> getInstances() {
        return instances;
    }

    public void removeInstance(InstanceEntity instance) {
        Platform.runLater(() -> {
            Kernel32.INSTANCE.CloseHandle(instance.getJob());
            instances.remove(instance);
            Set<InstanceEntity> instanceSet = started.get(instance.getApp());
            logger.info("instanceSet={}", instanceSet);
            if(instanceSet != null) {
                instanceSet.remove(instance);
                logger.info("instanceSetAfter={}", instanceSet);
                if(instanceSet.isEmpty()) {
                    started.remove(instance.getApp());
                }
            }
        });
    }

    private PROCESS_INFORMATION createProcess(AppEntity entity) {
        String commandLine = createCommandLine(entity);

        return processTrackerService.launch(
                commandLine,
                entity.getWorkingDirectory().toString()
        );
    }
    private String createCommandLine(AppEntity entity) {
        StringBuilder cmd = new StringBuilder();

        appendArgument(cmd, entity.getPath().toString());

        for (String arg : entity.getArguments()) {
            cmd.append(' ');
            appendArgument(cmd, arg);
        }

        logger.debug("Process start info: {}", cmd);

        return cmd.toString();
    }

    private void appendArgument(StringBuilder cmd, String arg) {
        if (arg.contains(" ") || arg.contains("\"")) {
            cmd.append('"')
                    .append(arg.replace("\"", "\\\""))
                    .append('"');
        } else {
            cmd.append(arg);
        }
    }
}
