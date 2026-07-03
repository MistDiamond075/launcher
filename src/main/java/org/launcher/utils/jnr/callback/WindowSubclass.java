package org.launcher.utils.jnr.callback;

import jnr.ffi.annotations.Delegate;
import jnr.ffi.types.uintptr_t;

public interface WindowSubclass {
    @Delegate
    @uintptr_t
    long invoke(
            @uintptr_t long hwnd,
            int uMsg,
            @uintptr_t  long wParam,
            @uintptr_t  long lParam,
            @uintptr_t  long uIdSubclass,
            @uintptr_t  long dwRefData
    );
}
