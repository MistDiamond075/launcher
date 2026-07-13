package org.launcher.utils.jnr.struct;

import jnr.ffi.Struct;

public class BITMAPINFOHEADER extends Struct {
    public final Unsigned32 size = new Unsigned32();
    public final Signed32 width = new Signed32();
    public final Signed32 height = new Signed32();
    public final Unsigned16 planes = new Unsigned16();
    public final Unsigned16 bitCount = new Unsigned16();
    public final Unsigned32 compression = new Unsigned32();

    public final byte[] rest = new byte[20];

    public BITMAPINFOHEADER(jnr.ffi.Runtime runtime) {
        super(runtime);
    }
}
