package org.launcher.controller;

import javafx.application.Platform;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import org.launcher.MainApp;
import org.launcher.async.AdminSessionControlAsync;
import org.launcher.utils.jnr.callback.LowLevelKeyboardProc;
import org.launcher.utils.jnr.lib.Kernel32;
import org.launcher.utils.jnr.lib.User32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.*;

import static org.launcher.utils.constants.KeyboardEventConstants.*;

public class KeyboardController {
    private static final Logger logger = LoggerFactory.getLogger(KeyboardController.class);
    private Pointer hook;
    private LowLevelKeyboardProc proc;
    private final Set<Integer> pressed = ConcurrentHashMap.newKeySet();
    private final Set<Integer> hotkey;
    private int hookThreadId;
    private final MainApp mainApp;
    private final byte[] keyState = new byte[256];
    private final char[] unicodeBuffer = new char[8];
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public KeyboardController(Set<Integer> hotkey, MainApp mainApp) {
        this.hotkey = Set.copyOf(hotkey);
        this.mainApp = mainApp;
    }

    public void start() {
        proc = (nCode, wParam, lParam) -> {
            if (nCode >= 0) {
                int vk = normalizeVk(lParam.getInt(0));
                int vkRaw = lParam.getInt(0);
                int scan = lParam.getInt(4);

                switch ((int) wParam.address()) {
                    case WM_SYSKEYDOWN,WM_KEYDOWN -> {
                        boolean firstPress = pressed.add(vk);
                        Set<Integer> snapshot = Set.copyOf(pressed);
                        if (vk == 0x14 && firstPress) {
                            keyState[0x14] ^= 0x01;
                        }
                        keyState[vk] |= (byte) 0x80;
                        byte[] keyStateCopy = keyState.clone();
                        Runnable task = () -> {
                            AdminSessionControlAsync.delayTermination();
                            logger.debug("firstpress={}, vk={},pressed={}, hotkey={}", firstPress, vk,snapshot, hotkey);
                            if (firstPress && snapshot.equals(hotkey)) {
                                try {
                                    Platform.runLater(() -> {
                                        if (mainApp.getRootId().equals("admin")) {
                                            mainApp.getAdminController().makeAdminMenuActive(false);
                                            mainApp.reloadScene(null);
                                        } else {
                                            mainApp.reloadScene("admin");
                                        }
                                    });
                                } catch (Exception e) {
                                    logger.error("Failed to reload scene");
                                    logger.debug("Details: ", e);
                                }
                            }
                            if(mainApp.getRootId().equals("admin") && mainApp.getAdminController().getPasswordScreen().isVisible()) {
                                redirectInput(vkRaw,scan,keyStateCopy);
                            }
                        };
                        executor.execute(task);
                    }

                    case WM_SYSKEYUP, WM_KEYUP -> {
                        pressed.remove(vk);
                        keyState[vk] &= (byte) ~0x80;
                    }
                }
            }

            return User32.INSTANCE.CallNextHookEx(
                    hook,
                    nCode,
                    wParam,
                    lParam
            );
        };

        hook = User32.INSTANCE.SetWindowsHookExW(
                WH_KEYBOARD_LL,
                proc,
                null,
                0
        );

        if (hook == null || Pointer.wrap(jnr.ffi.Runtime.getSystemRuntime(), 0).equals(hook)) {
            throw new IllegalStateException("SetWindowsHookExW failed");
        }

        Pointer msg = Memory.allocate(
                jnr.ffi.Runtime.getSystemRuntime(),
                48
        );

        hookThreadId = Kernel32.INSTANCE.GetCurrentThreadId();
        while (User32.INSTANCE.GetMessageW(msg, null, 0, 0) > 0) {
        }
    }

    public void stop() {
        if (hook != null) {
            User32.INSTANCE.UnhookWindowsHookEx(hook);
            User32.INSTANCE.PostThreadMessageW(
                    hookThreadId,
                    0x0012,
                    0,
                    0
            );
            hook = null;
        }
        logger.debug("Stopping keyboard controller executor");
        executor.shutdown();
        try {
            if(!executor.awaitTermination(5L,TimeUnit.SECONDS)){
                logger.debug("Forcing stop keyboard controller executor");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int normalizeVk(int vk) {
        return switch (vk) {
            case 0xA0, 0xA1 -> 0x10;
            case 0xA2, 0xA3 -> 0x11;
            case 0xA4, 0xA5 -> 0x12;
            default -> vk;
        };
    }

    private void redirectInput(int vkRaw, int scan, byte[] key_state){
        String text = vkToUnicode(
                vkRaw,
                scan,
                key_state
        );

        if (mainApp.getAdminController() != null) {
            mainApp.getAdminController().appendInput(INTKEYS_STRKEYS.getOrDefault(vkRaw,text));
        }
    }

    private String vkToUnicode(int vk, int scan, byte[] key_state) {
        int len = User32.INSTANCE.ToUnicodeEx(
                vk,
                scan,
                key_state,
                unicodeBuffer,
                unicodeBuffer.length,
                0,
                User32.INSTANCE.GetKeyboardLayout(0)
        );
        return len > 0 ? new String(unicodeBuffer, 0, len) : null;
    }
}
