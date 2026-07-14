package org.launcher.utils.jnr.lib;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Encoding;

public interface Shell32 {
    Shell32 INSTANCE = LibraryLoader.create(Shell32.class)
            .stdcall()
            .load("shell32");

    int ExtractIconExW(
            @Encoding("UTF-16LE")
            String file,
            int iconIndex,
            Pointer largeIcons,
            Pointer smallIcons,
            int iconCount
    );

    Pointer ExtractIconW(
            Pointer hwnd,
            String file,
            int index
    );


}
