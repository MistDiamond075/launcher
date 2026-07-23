package org.launcher.utils.jnr.struct;

import jnr.ffi.Struct;
import jnr.ffi.Runtime;

public class JOBOBJECT_ASSOCIATE_COMPLETION_PORT extends Struct {
    public final Pointer CompletionKey = new Pointer();
    public final Pointer CompletionPort = new Pointer();

    public JOBOBJECT_ASSOCIATE_COMPLETION_PORT(Runtime runtime) {
        super(runtime);
    }
}
