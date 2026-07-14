package org.launcher.utils.jnr.lib;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.byref.PointerByReference;
import org.launcher.utils.jnr.struct.BITMAPINFO;

public interface Gdi32 {
    Gdi32 INSTANCE = LibraryLoader.create(Gdi32.class)
            .stdcall()
            .load("gdi32");
    int BI_RGB = 0;
    int DIB_RGB_COLORS = 0;

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

    int GetObjectW(
            Pointer hgdiobj,
            int cbBuffer,
            Pointer object
    );

    Pointer CreateDIBSection(
            Pointer hdc,
            BITMAPINFO bitmapInfo,
            int usage,
            PointerByReference bits,
            Pointer section,
            int offset
    );
}
