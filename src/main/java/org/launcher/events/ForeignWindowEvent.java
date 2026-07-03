package org.launcher.events;


import javafx.application.Platform;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import org.launcher.async.CloseWindowAsync;
import org.launcher.entity.InstanceEntity;
import org.launcher.utils.jnr.lib.Kernel32;
import org.launcher.utils.jnr.lib.User32;
import org.launcher.utils.WindowEventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.launcher.utils.WindowEventConstants.*;

public class ForeignWindowEvent implements AutoCloseable{
    private static final Logger logger = LoggerFactory.getLogger(ForeignWindowEvent.class);

    private final User32 user32 = User32.INSTANCE;
    private final Map<Long, InstanceEntity> byPid = new ConcurrentHashMap<>();
    private volatile Thread hookThread;
    private volatile int hookThreadId;
    private volatile boolean running;
    private volatile Pointer startHook;
    private volatile Pointer showHideHook;
    private volatile Pointer foregroundHook;
    private volatile Pointer minimizeHook;
    private volatile User32.WinEventProc callback;
    private final Kernel32 kernel32 = Kernel32.INSTANCE;
    private final CloseWindowAsync closeWindowScheduler = new CloseWindowAsync();

    public ForeignWindowEvent() {
        this.callback = this::onWinEvent;
    }

    public synchronized void start() {
        if (hookThread != null) {
            return;
        }

        running = true;
        hookThread = new Thread(this::runLoop, "win-event-hook");
        hookThread.setDaemon(true);
        hookThread.start();
    }

    public synchronized void stop() {
        running = false;
        int id = hookThreadId;
        if (id != 0) {
            user32.PostThreadMessageW(id, WM_QUIT, 0, 0);
        }
    }

    public void register(InstanceEntity instance) {
        byPid.put(instance.getProcess().pid(), instance);
    }

    public void unregister(long pid) {
        byPid.remove(pid);
    }

    private void onWinEvent(
            Pointer hWinEventHook,
            int event,
            Pointer hwndPtr,
            int idObject,
            int idChild,
            int idEventThread,
            int dwmsEventTime
    ) {
        if (hwndPtr == null || hwndPtr.address() == 0) {
            return;
        }

        if (idObject != WindowEventConstants.OBJID_WINDOW
                || idChild != WindowEventConstants.CHILDID_SELF) {
            return;
        }

        int[] pidArr = new int[1];
        user32.GetWindowThreadProcessId(hwndPtr, pidArr);

        long pid = Integer.toUnsignedLong(pidArr[0]);
        long hwnd = hwndPtr.address();

        InstanceEntity instance = byPid.get(pid);
        if (instance == null) {
            return;
        }

        if (event == EVENT_OBJECT_SHOW) {
            Pointer parent = user32.GetParent(hwndPtr);
            boolean visible = user32.IsWindowVisible(hwndPtr) != 0;
            boolean topLevel = parent == null || parent.address() == 0;
            if(visible && topLevel) {
                if (instance.getHwnds().add(hwnd)) {
                    Platform.runLater(() -> {
                        if (instance.getState() == InstanceEntity.State.STARTING) {
                            instance.setState(InstanceEntity.State.RUNNING);
                        }
                    });
                }
            }
            //Platform.runLater(() -> instance.setState(InstanceEntity.State.RUNNING));
            char[] title = new char[512];
            user32.GetWindowTextW(hwndPtr, title, title.length);
            logger.debug("Created window pid={}, hwnd={},title={},visible={},topLevel={},hwnds={}", pid, hwnd, new String(title).trim(), visible, topLevel,instance.getHwnds());
        }
        else if (event == WindowEventConstants.EVENT_SYSTEM_FOREGROUND) {
            if(instance.getHwnds().contains(hwnd)) {
                Platform.runLater(() -> instance.setForeground(true));
            }
            logger.debug("Foreground window pid={}, hwnd={}", pid, hwnd);
        }
        else if(event == EVENT_OBJECT_DESTROY){
            instance.getHwnds().remove(hwnd);
            if(!instance.getHwnds().isEmpty() && instance.getState() != InstanceEntity.State.STARTING) {
                closeWindowScheduler.scheduleClose(instance);
            }else{
              //  Platform.runLater(() -> instance.setState(InstanceEntity.State.CLOSED));
                if(instance.getState() != InstanceEntity.State.STARTING)
                closeWindowScheduler.scheduleClose(instance);
            }
            logger.debug("Destroyed window: pid={}, hwnd={}, hwnds={}", pid, hwnd,instance.getHwnds());
        }
        else if(event == EVENT_SYSTEM_MINIMIZESTART){
            if(instance.getHwnds().contains(hwnd)) {
                Platform.runLater(() -> instance.setMinimized(true));
            }
        }
        else if(event == EVENT_SYSTEM_MINIMIZEEND){
            if(instance.getHwnds().contains(hwnd)) {
                Platform.runLater(() -> instance.setMinimized(false));
            }
        }
    }

    private void runLoop() {
        final jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getRuntime(user32);
        final Pointer msg = Memory.allocateDirect(runtime, 64, true);

        user32.PeekMessageW(msg, null, 0, 0, WINEVENT_OUTOFCONTEXT);

        hookThreadId = kernel32.GetCurrentThreadId();

        callback = this::onWinEvent;
        startHook = user32.SetWinEventHook(
                EVENT_OBJECT_CREATE,
                EVENT_OBJECT_DESTROY,
                null,
                callback,
                0,
                0,
                WINEVENT_OUTOFCONTEXT
        );

        showHideHook = user32.SetWinEventHook(
                EVENT_OBJECT_SHOW,
                EVENT_OBJECT_HIDE,
                null,
                callback,
                0, 0,
                WINEVENT_OUTOFCONTEXT
        );

        foregroundHook = user32.SetWinEventHook(
                EVENT_SYSTEM_FOREGROUND,
                EVENT_SYSTEM_FOREGROUND,
                null,
                callback,
                0, 0,
                WINEVENT_OUTOFCONTEXT
        );

        minimizeHook = user32.SetWinEventHook(
                EVENT_SYSTEM_MINIMIZESTART,
                EVENT_SYSTEM_MINIMIZEEND,
                null,
                callback,
                0, 0,
                WINEVENT_OUTOFCONTEXT
        );

        if (startHook == null || startHook.address() == 0) {
            running = false;
            return;
        }

        while (running) {
            int rc = user32.GetMessageW(msg, null, 0, 0);
            if (rc == 0) {   // WM_QUIT
                break;
            }
            if (rc < 0) {
                break;
            }
            user32.TranslateMessage(msg);
            user32.DispatchMessageW(msg);
        }

        unhook(showHideHook);
        unhook(foregroundHook);
        unhook(minimizeHook);
        unhook(startHook);

        closeWindowScheduler.stop();

        hookThread = null;
        hookThreadId = 0;
    }

    @Override
    public void close() {
        stop();
    }

    private void unhook(Pointer hook){
        if(hook != null && hook.address() != 0) {
            user32.UnhookWinEvent(hook);
            hook = null;
        }
    }
}
