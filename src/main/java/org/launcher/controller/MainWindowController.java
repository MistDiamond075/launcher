package org.launcher.controller;

import org.launcher.async.AdminSessionControlAsync;
import org.launcher.utils.jnr.lib.ComCtl32;
import org.launcher.utils.jnr.callback.WindowSubclass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.launcher.utils.constants.WindowEventConstants.*;

public class MainWindowController {
    private static final Logger logger = LoggerFactory.getLogger(MainWindowController.class);
    private final WindowSubclass subclassProc = this::windowProc;
    private long hwnd;
    private static final long SUBCLASS_ID = 1;

    public void setHwnd(long hwnd){
        this.hwnd = hwnd;

        if (ComCtl32.INSTANCE.SetWindowSubclass(
                hwnd,
                subclassProc,
                SUBCLASS_ID,
                0
        ) == 0) {
            throw new IllegalStateException("SetWindowSubclass failed");
        }
    }

    public void removeHwnd(){
        if (hwnd == 0) {
            return;
        }

        ComCtl32.INSTANCE.RemoveWindowSubclass(
                hwnd,
                subclassProc,
                SUBCLASS_ID
        );

        hwnd = 0;
    }

    private long windowProc(
            long hwnd,
            int msg,
            long wParam,
            long lParam,
            long uIdSubclass,
            long dwRefData
    ) {
        switch (msg) {

            case WM_MOUSEACTIVATE -> {
             //   logger.debug("Mouse activated");
                AdminSessionControlAsync.delayTermination();
                return MA_NOACTIVATE;
            }

            case WM_NCDESTROY -> {
                return ComCtl32.INSTANCE.RemoveWindowSubclass(
                        hwnd,
                        subclassProc,
                        SUBCLASS_ID
                );
            }

            default -> {
                return ComCtl32.INSTANCE.DefSubclassProc(
                        hwnd, msg, wParam, lParam
                );
            }
        }
    }
}
