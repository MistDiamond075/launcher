package org.launcher.utils.jnr.lib;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;

public interface Shell32 {
    Shell32 INSTANCE = LibraryLoader.create(Shell32.class)
            .library("shell32")
            .load();

    int ExtractIconExW(
            String file,
            int iconIndex,
            Pointer largeIcons,
            Pointer smallIcons,
            int iconCount
    );
}
