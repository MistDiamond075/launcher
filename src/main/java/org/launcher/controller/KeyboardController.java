package org.launcher.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import java.util.concurrent.ConcurrentHashMap;

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

    public KeyboardController(Set<Integer> hotkey, MainApp mainApp) {
        this.hotkey = Set.copyOf(hotkey);
        this.mainApp = mainApp;
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
                        if (vk == 0x14 && keyState[0x14] == 0) {
                            keyState[0x14] ^= 0x01;
                        }else if (vk == 0x14){
                            keyState[0x14] = 0;
                        }
                        boolean firstPress = pressed.add(vk);
                        keyState[vk] |= (byte) 0x80;
                        AdminSessionControlAsync.delayTermination();
                        logger.debug("firstpress={}, vk={},pressed={}, hotkey={}", firstPress, vk,pressed, hotkey);
                        if (firstPress && pressed.equals(hotkey)) {
                            try {
                                //NotificationService.show("app.shutdown","Shutting down...", null,BaseException.Type.INFO);
                               // Platform.runLater(Platform::exit);
                                Platform.runLater(() -> {
                                    if (mainApp.getRootId().equals("admin")) {
                                        mainApp.reloadScene(null);
                                        mainApp.getAdminController().makeAdminMenuActive(false);
                                    } else {
                                        mainApp.reloadScene("admin");
                                    }
                                });
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if(mainApp.getRootId().equals("admin") && mainApp.getAdminController().getPasswordScreen().isVisible()) {
                            redirectInput(lParam);
                        }
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
    }

    private int normalizeVk(int vk) {
        return switch (vk) {
            case 0xA0, 0xA1 -> 0x10; // LSHIFT, RSHIFT -> SHIFT
            case 0xA2, 0xA3 -> 0x11; // LCTRL, RCTRL -> CONTROL
            case 0xA4, 0xA5 -> 0x12; // LALT, RALT -> MENU
            default -> vk;
        };
    }

    private void redirectInput(Pointer lParam){
        String text = vkToUnicode(
                lParam.getInt(0),
                lParam.getInt(4)
        );

        if (mainApp.getAdminController() != null) {
            switch(normalizeVk(lParam.getInt(0))) {
                case 13 ->  mainApp.getAdminController().appendInput("ENTER");
                case 8 -> mainApp.getAdminController().appendInput("BACKSPACE");
                case 32 -> mainApp.getAdminController().appendInput("SPACE");
                case 46 -> mainApp.getAdminController().appendInput("DELETE");
                case 39 -> mainApp.getAdminController().appendInput("R_ARROW");
                case 37 -> mainApp.getAdminController().appendInput("L_ARROW");
                default -> {
                    if(text != null) {
                        mainApp.getAdminController().appendInput(text);
                    }
                }
            }
        }
    }

    private String vkToUnicode(int vk, int scan) {
        char[] buffer = new char[8];

        int len = User32.INSTANCE.ToUnicodeEx(
                vk,
                scan,
                keyState,
                buffer,
                buffer.length,
                0,
                User32.INSTANCE.GetKeyboardLayout(0)
        );

        if (len > 0) {
            return new String(buffer, 0, len);
        }

        return null;
    }
}
