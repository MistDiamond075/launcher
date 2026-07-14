package org.launcher.utils.jnr.struct;

import jnr.ffi.Struct;

public class BITMAP extends Struct {
    public final Signed32 bmType = new Signed32();
    public final Signed32 bmWidth = new Signed32();
    public final Signed32 bmHeight = new Signed32();
    public final Signed32 bmWidthBytes = new Signed32();
    public final Unsigned16 bmPlanes = new Unsigned16();
    public final Unsigned16 bmBitsPixel = new Unsigned16();
    public final Pointer bmBits = new Pointer();

    public BITMAP(jnr.ffi.Runtime runtime) {
        super(runtime);
    }
}
