package org.launcher.utils.jnr.lib;

import jnr.ffi.LibraryLoader;

public interface Kernel32 {
    Kernel32 INSTANCE = LibraryLoader.create(Kernel32.class).load("kernel32");
    int GetCurrentThreadId();
}
