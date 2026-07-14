package org.launcher.utils.icons;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.byref.PointerByReference;
import org.launcher.utils.jnr.lib.Gdi32;
import org.launcher.utils.jnr.struct.BITMAPINFO;

public class DibSection {
    public final Pointer hdc;
    public final Pointer hBitmap;
    public final Pointer oldBitmap;
    public final Pointer bits;
    public final int width;
    public final int height;

    public DibSection(Pointer hdc,
                      Pointer hBitmap,
                      Pointer oldBitmap,
                      Pointer bits,
                      int width,
                      int height) {
        this.hdc = hdc;
        this.hBitmap = hBitmap;
        this.oldBitmap = oldBitmap;
        this.bits = bits;
        this.width = width;
        this.height = height;
    }

    public static DibSection createDibSection(int width, int height) {
        Pointer hdc = Gdi32.INSTANCE.CreateCompatibleDC(null);

        BITMAPINFO bmi = new BITMAPINFO(Runtime.getSystemRuntime());
        bmi.bmiHeader.biSize.set(Struct.size(bmi.bmiHeader));
        bmi.bmiHeader.biWidth.set(width);
        bmi.bmiHeader.biHeight.set(-height);
        bmi.bmiHeader.biPlanes.set(1);
        bmi.bmiHeader.biBitCount.set(32);
        bmi.bmiHeader.biCompression.set(0);

        PointerByReference bitsRef = new PointerByReference();

        Pointer hBitmap = Gdi32.INSTANCE.CreateDIBSection(
                hdc,
                bmi,
                Gdi32.DIB_RGB_COLORS,
                bitsRef,
                null,
                0
        );

        Pointer oldBitmap = Gdi32.INSTANCE.SelectObject(hdc, hBitmap);

        return new DibSection(
                hdc,
                hBitmap,
                oldBitmap,
                bitsRef.getValue(),
                width,
                height
        );
    }
}