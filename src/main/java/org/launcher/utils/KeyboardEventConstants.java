package org.launcher.utils;

import java.util.Map;

public class KeyboardEventConstants {
    public static final int WH_KEYBOARD_LL = 13;

    public static final int WM_KEYDOWN = 0x0100;
    public static final int WM_KEYUP = 0x0101;
    public static final int WM_SYSKEYDOWN = 0x0104;
    public static final int WM_SYSKEYUP = 0x0105;
    public static final Map<String, Integer> KEYS = Map.ofEntries(
            Map.entry("CTRL", 0x11),
            Map.entry("ALT", 0x12),
            Map.entry("SHIFT", 0x10),
            Map.entry("WIN", 0x5B),
            Map.entry("SPACE", 0x20),
            Map.entry("TAB", 0x09),
            Map.entry("ENTER", 0x0D),
            Map.entry("ESC", 0x1B),
            Map.entry("LEFT", 0x25),
            Map.entry("UP", 0x26),
            Map.entry("RIGHT", 0x27),
            Map.entry("DOWN", 0x28)
    );
}
