package org.launcher.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localization {
    private static final Logger logger = LoggerFactory.getLogger(Localization.class);
    private static ResourceBundle messages;

    public static void load() {
        try {
            messages = ResourceBundle.getBundle(
                    "messages",
                    Locale.forLanguageTag("ru"));
        } catch (MissingResourceException e) {
            logger.error("Failed to load localization messages");
            logger.debug(String.valueOf(e));
        }
//        try (InputStream in = MainApp.class.getClassLoader().getResourceAsStream("messages.lc")) {
//            messages.load(in);
//            loaded = true;
//        } catch (IOException e) {
//            logger.error("Failed to load localization messages");
//            logger.debug(String.valueOf(e));
//            loaded = false;
//        }
    }

    public static String get(String key) {
        return get(key, "undefined string");
    }

    public static String get(String key, String defaultValue) {
        try {
            if(messages != null) {
                return messages.getString(key);
            }else{
                return defaultValue;
            }
        }catch (MissingResourceException e){
            return defaultValue;
        }
    }
}
