package org.launcher.utils;

import jnr.ffi.Memory;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import org.launcher.utils.jnr.lib.Shell32;

public class IconManager {
    public static long extractHicon(){
        jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getSystemRuntime();

        Pointer large = Memory.allocateDirect(runtime, NativeType.ADDRESS);
        large.putAddress(0, 0);

        int result = Shell32.INSTANCE.ExtractIconExW(
                "C:\\Program Files\\App\\app.exe",
                0,
                large,
                null,
                1
        );

        if (result == 1) {
            return  large.getAddress(0);
        }
        return -1;
    }
}
