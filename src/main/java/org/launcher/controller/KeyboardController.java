package org.launcher.controller;

import javafx.application.Platform;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import org.launcher.exception.BaseException;
import org.launcher.service.NotificationService;
import org.launcher.utils.jnr.callback.LowLevelKeyboardProc;
import org.launcher.utils.jnr.lib.Kernel32;
import org.launcher.utils.jnr.lib.User32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.launcher.utils.constants.KeyboardEventConstants.*;

public class KeyboardController {
    private static final Logger logger = LoggerFactory.getLogger(KeyboardController.class);
    private Pointer hook;
    private LowLevelKeyboardProc proc;
    private final Set<Integer> pressed = ConcurrentHashMap.newKeySet();
    private final Set<Integer> hotkey;
    private int hookThreadId;

    public KeyboardController(Set<Integer> hotkey) {
        this.hotkey = Set.copyOf(hotkey);
    }

    public void start() {
        proc = (nCode, wParam, lParam) -> {
            logger.debug(
                    "msg={}, vk={}, scan={}, flags={}",
                    wParam.address(),
                    lParam.getInt(0),
                    lParam.getInt(4),
                    lParam.getInt(8)
            );
            if (nCode >= 0) {
                int vk = normalizeVk(lParam.getInt(0));

                switch ((int) wParam.address()) {
                    case WM_SYSKEYDOWN,WM_KEYDOWN -> {
                        boolean firstPress = pressed.add(vk);
                        logger.debug("firstpress={}, vk={},pressed={}, hotkey={}", firstPress, vk,pressed, hotkey);
                        if (firstPress && pressed.equals(hotkey)) {
                            try {
                                NotificationService.show("app.shutdown","Shutting down...", null,BaseException.Type.INFO);
                                Platform.runLater(Platform::exit);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    case WM_SYSKEYUP, WM_KEYUP -> pressed.remove(vk);
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
    }

    private int normalizeVk(int vk) {
        return switch (vk) {
            case 0xA0, 0xA1 -> 0x10; // LSHIFT, RSHIFT -> SHIFT
            case 0xA2, 0xA3 -> 0x11; // LCTRL, RCTRL -> CONTROL
            case 0xA4, 0xA5 -> 0x12; // LALT, RALT -> MENU
            default -> vk;
        };
    }
}
