package org.launcher.utils.jnr.struct;

import jnr.ffi.Struct;

public class BITMAPINFO extends Struct {
    public final BITMAPINFOHEADER bmiHeader;

    public BITMAPINFO(jnr.ffi.Runtime runtime) {
        super(runtime);
        bmiHeader = new BITMAPINFOHEADER(runtime);
    }
}
