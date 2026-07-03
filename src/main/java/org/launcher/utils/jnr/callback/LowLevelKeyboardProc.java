package org.launcher.utils.jnr.callback;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;

public interface LowLevelKeyboardProc {
    @Delegate
    Pointer invoke(int nCode, Pointer wParam, Pointer lParam);
}
