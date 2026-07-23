package org.launcher.utils.jnr.struct;

import jnr.ffi.Struct;
import jnr.ffi.Runtime;

public class STARTUPINFO extends Struct {
    public final DWORD cb = new DWORD();

    public final Pointer lpReserved = new Pointer();
    public final Pointer lpDesktop = new Pointer();
    public final Pointer lpTitle = new Pointer();

    public final DWORD dwX = new DWORD();
    public final DWORD dwY = new DWORD();
    public final DWORD dwXSize = new DWORD();
    public final DWORD dwYSize = new DWORD();
    public final DWORD dwXCountChars = new DWORD();
    public final DWORD dwYCountChars = new DWORD();
    public final DWORD dwFillAttribute = new DWORD();
    public final DWORD dwFlags = new DWORD();

    public final WORD wShowWindow = new WORD();
    public final WORD cbReserved2 = new WORD();

    public final Pointer lpReserved2 = new Pointer();

    public final Pointer hStdInput = new Pointer();
    public final Pointer hStdOutput = new Pointer();
    public final Pointer hStdError = new Pointer();


    public STARTUPINFO(Runtime runtime) {
        super(runtime);
        //cb.set(size(this));
    }
}
