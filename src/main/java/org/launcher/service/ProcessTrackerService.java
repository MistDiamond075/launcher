package org.launcher.service;

import javafx.application.Platform;
import jnr.ffi.*;
import jnr.ffi.Runtime;
import org.launcher.entity.InstanceEntity;
import org.launcher.utils.jnr.lib.Kernel32;
import org.launcher.utils.jnr.struct.JOBOBJECT_ASSOCIATE_COMPLETION_PORT;
import org.launcher.utils.jnr.struct.PROCESS_INFORMATION;
import org.launcher.utils.jnr.struct.STARTUPINFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.launcher.utils.constants.JobMessageConstants.*;

public class ProcessTrackerService implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ProcessTrackerService.class);
    private final Pointer port;
    private final int JobObjectAssociateCompletionPortInformation = 7;
    private final int CREATE_SUSPENDED = 0x00000004;
    private Thread completionThread;
    private final Map<Pointer, InstanceEntity> jobs_instances = new ConcurrentHashMap<>();

    public ProcessTrackerService() {
        port = Kernel32.INSTANCE.CreateIoCompletionPort(
                Pointer.wrap(Runtime.getSystemRuntime(), -1),
                null,
                null,
                1
        );

        if (port == null || port.address() == 0) {
            throw new RuntimeException("Failed to create IO Completion Port");
        }
        start();
    }

    public PROCESS_INFORMATION launch(String path, String workingDir) {
        Runtime runtime = Runtime.getSystemRuntime();
        STARTUPINFO startup = new STARTUPINFO(runtime);
        PROCESS_INFORMATION pi = new PROCESS_INFORMATION(runtime);
        byte[] bytes = (path + '\0').getBytes(StandardCharsets.UTF_16LE);
        Pointer cmd = Memory.allocateDirect(runtime, bytes.length);
        cmd.put(0, bytes, 0, bytes.length);
        byte[] bytes2 = (workingDir + '\0').getBytes(StandardCharsets.UTF_16LE);
        Pointer workingd = Memory.allocateDirect(runtime, bytes2.length);
        workingd.put(0, bytes2, 0, bytes2.length);
        startup.cb.set(Struct.size(startup));
        int result = Kernel32.INSTANCE.CreateProcessW(
                        null,
                        cmd,
                        null,
                        null,
                        false,
                        CREATE_SUSPENDED,
                        null,
                        workingd,
                        Struct.getMemory(startup),
                        Struct.getMemory(pi)
                );
        if(result == 0) {
            throw new RuntimeException("CreateProcess failed: "+ Kernel32.INSTANCE.GetLastError());
        }
        return pi;
    }

    public Pointer createJob(){
        Pointer hJob = Kernel32.INSTANCE.CreateJobObjectW(null, null);
        if (hJob == null || hJob.address() == 0) {
            throw new RuntimeException(
                    "CreateJobObject failed: " + Kernel32.INSTANCE.GetLastError());
        }
        assignJob(hJob);
        return hJob;
    }

    public void registerInstance(InstanceEntity instance) {
        jobs_instances.put(
                instance.getJob(),
                instance
        );
    }

    private void assignJob(Pointer hJob){
        JOBOBJECT_ASSOCIATE_COMPLETION_PORT info =
                new JOBOBJECT_ASSOCIATE_COMPLETION_PORT(Runtime.getSystemRuntime());

        info.CompletionKey.set(hJob);
        info.CompletionPort.set(port);

        int ok = Kernel32.INSTANCE.SetInformationJobObject(
                hJob,
                JobObjectAssociateCompletionPortInformation,
                Struct.getMemory(info),
                Struct.size(info)
        );
        if (ok == 0) {
            throw new RuntimeException("SetInformationJobObject failed: " + Kernel32.INSTANCE.GetLastError());
        }
    }

    public void start(){
        completionThread = new Thread(() -> {
            Runtime runtime = Runtime.getSystemRuntime();
            Pointer bytes = Memory.allocateDirect(runtime, 8);
            Pointer key = Memory.allocateDirect(runtime, TypeAlias.size_t);
            Pointer overlapped = Memory.allocateDirect(runtime, TypeAlias.size_t);

            while (!Thread.currentThread().isInterrupted()) {
                int ok = Kernel32.INSTANCE.GetQueuedCompletionStatus(
                        port,
                        bytes,
                        key,
                        overlapped,
                        -1
                );

                if (ok == 0) {
                    continue;
                }
                long message = bytes.getLongLong(0);
                if(message == 0){
                    break;
                }
                Pointer job = key.getPointer(0);
                long pid = overlapped.getAddress(0);
                logger.debug(
                        "message={}, key={}, overlapped={}",
                        message,
                        key.getPointer(0),
                        overlapped.getPointer(0)
                );
                switch ((int) message) {
                    case JOB_OBJECT_MSG_EXIT_PROCESS -> logger.debug("Process exited, pid={}", pid);
                    case  JOB_OBJECT_MSG_ABNORMAL_EXIT_PROCESS -> {
                        InstanceEntity instance = jobs_instances.remove(job);
                        logger.warn("Process crashed, pid={}", pid);
                        if (instance != null && !jobs_instances.containsValue(instance)) {
                            Platform.runLater(() -> instance.setState(InstanceEntity.State.CLOSING));
                        }
                    }
                    case JOB_OBJECT_MSG_NEW_PROCESS -> logger.debug("New process in job, pid={}", pid);
                    case JOB_OBJECT_MSG_ACTIVE_PROCESS_ZERO -> {
                        InstanceEntity instance = jobs_instances.remove(job);
                        if (instance != null && jobs_instances.values().stream().noneMatch(i -> i.getApp().equals(instance.getApp()))) {
                            instance.setState(InstanceEntity.State.CLOSED);
                        }
                        if(instance != null) {
                            Kernel32.INSTANCE.CloseHandle(instance.getProcess().hProcess.get());
                        }
                        logger.debug("Job has no active processes");
                    }
                }
            }
        });
        completionThread.setDaemon(true);
        completionThread.setName("JobCompletionThread");
        completionThread.start();
    }

    @Override
    public void close() {
        Kernel32.INSTANCE.PostQueuedCompletionStatus(
                port,
                0,
                null,
                null
        );
        completionThread.interrupt();
        Kernel32.INSTANCE.CloseHandle(port);
    }
}
