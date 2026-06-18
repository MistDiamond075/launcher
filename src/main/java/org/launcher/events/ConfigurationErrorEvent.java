package org.launcher.events;

import javafx.event.Event;
import javafx.event.EventType;

public class ConfigurationErrorEvent extends Event {
    public static final EventType<ConfigurationErrorEvent> err =
            new EventType<>(Event.ANY, "CONFIGURATION_ERROR");

    public ConfigurationErrorEvent() {
        super(err);
    }

}
