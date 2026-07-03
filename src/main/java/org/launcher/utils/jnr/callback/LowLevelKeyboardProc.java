package org.launcher.utils.jnr.callback;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.types.uintptr_t;

public interface LowLevelKeyboardProc {
    @Delegate
    Pointer invoke(int nCode, @uintptr_t long wParam, Pointer lParam);
}
