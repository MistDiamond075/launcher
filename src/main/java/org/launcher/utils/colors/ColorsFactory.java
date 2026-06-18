package org.launcher.utils.colors;

public class ColorsFactory {
    public static Colors getColors(String type){
        return switch (type) {
            case "html" -> new HtmlColors();
            case "ansi" -> new ANSIColors();
            default -> new ANSIColors();
        };
    }
}
