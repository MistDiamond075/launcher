package org.launcher.utils;

public class WindowEventConstants {
    public static final int EVENT_OBJECT_CREATE = 0x8000;
    public static final int EVENT_OBJECT_DESTROY = 0x8001;
    public static final int EVENT_SYSTEM_FOREGROUND = 0x0003;

    public static final int OBJID_WINDOW = 0x00000000;
    public static final int CHILDID_SELF = 0;
    public static final int WINEVENT_OUTOFCONTEXT = 0x0000;
    public static final int WM_QUIT = 0x0012;
    public static final int EVENT_OBJECT_SHOW    = 0x8002;
    public static final int EVENT_OBJECT_HIDE    = 0x8003;
    public static final int EVENT_SYSTEM_MINIMIZESTART = 0x0016;
    public static final int EVENT_SYSTEM_MINIMIZEEND   = 0x0017;
    public static final int WM_MOUSEACTIVATE = 33;
    public static final int MA_ACTIVATE = 1;
    public static final int MA_ACTIVATEANDEAT = 2;
    public static final int MA_NOACTIVATE = 3;
    public static final int MA_NOACTIVATEANDEAT = 4;
    public static final int WM_NCDESTROY  = 130;
}
