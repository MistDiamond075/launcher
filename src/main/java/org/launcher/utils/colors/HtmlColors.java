package org.launcher.utils.colors;

public class HtmlColors extends Colors{

    @Override
    public String RESET() {
        return "</span>";
    }

    @Override
    public String BLACK() {
        return "<span style=\"color:black\">";
    }

    @Override
    public String RED() {
        return "<span style=\"color:red\">";
    }

    @Override
    public String GREEN() {
        return "<span style=\"color:green\">";
    }

    @Override
    public String YELLOW() {
        return "<span style=\"color:yellow\">";
    }

    @Override
    public String BLUE() {
        return "<span style=\"color:#0070ff\">";
    }

    @Override
    public String PURPLE() {
        return "<span style=\"color:PURPLE\">";
    }

    @Override
    public String CYAN() {
        return "<span style=\"color:CYAN\">";
    }

    @Override
    public String WHITE() {
        return "<span style=\"color:WHITE\">";
    }
}
