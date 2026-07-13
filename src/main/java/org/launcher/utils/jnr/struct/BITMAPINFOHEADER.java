package org.launcher.utils.jnr.struct;

import jnr.ffi.Struct;

public class BITMAPINFOHEADER extends Struct {
    public final Unsigned32 biSize = new Unsigned32();
    public final Signed32 biWidth = new Signed32();
    public final Signed32 biHeight = new Signed32();
    public final Unsigned16 biPlanes = new Unsigned16();
    public final Unsigned16 biBitCount = new Unsigned16();
    public final Unsigned32 biCompression = new Unsigned32();
    public final Unsigned32 biSizeImage = new Unsigned32();
    public final Signed32 biXPelsPerMeter = new Signed32();
    public final Signed32 biYPelsPerMeter = new Signed32();
    public final Unsigned32 biClrUsed = new Unsigned32();
    public final Unsigned32 biClrImportant = new Unsigned32();

    public BITMAPINFOHEADER(jnr.ffi.Runtime runtime) {
        super(runtime);
    }
}
