package org.launcher.utils.jnr.lib;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.byref.PointerByReference;
import org.launcher.utils.jnr.struct.BITMAPINFO;

public interface Gdi32 {
    Gdi32 INSTANCE = LibraryLoader.create(Gdi32.class)
            .stdcall()
            .load("gdi32");

    Pointer CreateCompatibleDC(Pointer hdc);

    Pointer SelectObject(Pointer hdc, Pointer object);

    boolean DeleteObject(Pointer object);

    boolean DeleteDC(Pointer hdc);

    int GetDIBits(
            Pointer hdc,
            Pointer hBitmap,
            int startScan,
            int scanLines,
            Pointer bits,
            BITMAPINFO bitmapInfo,
            int usage
    );
}
