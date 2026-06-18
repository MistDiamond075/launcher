package org.launcher.utils.colors;

public class ANSIColors extends Colors{
    @Override
    public String RESET() {
        return "\u001B[0m";
    }

    @Override
    public String BLACK() {
        return "\u001B[90m";
    }

    @Override
    public String RED() {
        return "\u001B[91m";
    }

    @Override
    public String GREEN() {
        return "\u001B[92m";
    }

    @Override
    public String YELLOW() {
        return "\u001B[93m";
    }

    @Override
    public String BLUE() {
        return "\u001B[94m";
    }

    @Override
    public String PURPLE() {
        return "\u001B[95m";
    }

    @Override
    public String CYAN() {
        return "\u001B[96m";
    }

    @Override
    public String WHITE() {
        return "\u001B[97m";
    }
}
