package org.launcher.utils.jnr.callback;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;

public interface InputProc {
    @Delegate
    Pointer invoke(int nCode, Pointer wParam, Pointer lParam);
}
