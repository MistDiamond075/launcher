package org.launcher.utils.jnr.struct;

import jnr.ffi.Pointer;
import jnr.ffi.Struct;

public class ICONINFO extends Struct {
    public final Struct.Boolean fIcon = new Struct.Boolean();
    public final Struct.Unsigned32 xHotspot = new Struct.Unsigned32();
    public final Struct.Unsigned32 yHotspot = new Struct.Unsigned32();
    public final Pointer hbmMask = new Pointer();
    public final Pointer hbmColor = new Pointer();

    public ICONINFO(jnr.ffi.Runtime runtime) {
        super(runtime);
    }
}
