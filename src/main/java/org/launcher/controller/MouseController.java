package org.launcher.controller;

import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import org.launcher.async.SessionControlAsync;
import org.launcher.utils.jnr.callback.InputProc;
import org.launcher.utils.jnr.lib.Kernel32;
import org.launcher.utils.jnr.lib.User32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.launcher.utils.constants.MouseEventConstants.*;

public class MouseController {
    private static final Logger logger = LoggerFactory.getLogger(MouseController.class);
    private Pointer hook;
    private InputProc proc;
    private int hookThreadId;

    public void start(){
        proc = (nCode, wParam, lParam) ->{
            if (nCode >= 0) {

                int msg = (int)wParam.address();

                switch (msg) {
                    case WM_MOUSEMOVE, WM_LBUTTONDOWN, WM_RBUTTONDOWN, WM_MOUSEWHEEL -> SessionControlAsync.delayTermination();
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
                WH_MOUSE_LL,
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
}
