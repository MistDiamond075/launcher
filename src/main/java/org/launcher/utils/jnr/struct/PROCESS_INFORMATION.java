package org.launcher.utils.jnr.struct;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class PROCESS_INFORMATION extends Struct {
    public final Pointer hProcess = new Pointer();
    public final Pointer hThread = new Pointer();

    public final Struct.Unsigned32 dwProcessId = new Unsigned32();
    public final Unsigned32 dwThreadId = new Unsigned32();


    public PROCESS_INFORMATION(Runtime runtime) {
        super(runtime);
    }
}
