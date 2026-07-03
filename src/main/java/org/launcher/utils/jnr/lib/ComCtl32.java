package org.launcher.utils.jnr.lib;

import jnr.ffi.LibraryLoader;
import jnr.ffi.types.uintptr_t;
import org.launcher.utils.jnr.callback.WindowSubclass;

public interface ComCtl32 {
    ComCtl32 INSTANCE =
            LibraryLoader.create(ComCtl32.class)
                    .load("comctl32");

    int SetWindowSubclass(
            @uintptr_t long hWnd,
            WindowSubclass proc,
            @uintptr_t long uIdSubclass,
            @uintptr_t long dwRefData);

    int RemoveWindowSubclass(
            @uintptr_t long hWnd,
            WindowSubclass proc,
            @uintptr_t long uIdSubclass);

    long DefSubclassProc(
            @uintptr_t long hWnd,
            int uMsg,
            @uintptr_t long wParam,
            @uintptr_t long lParam);
}
