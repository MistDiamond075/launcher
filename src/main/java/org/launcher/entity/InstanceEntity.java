package org.launcher.entity;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceEntity {
    public enum State{STARTING, RUNNING, CLOSING, CLOSED, FAILED}
    private final AppEntity app;
    private final Process process;
    private final Set<Long> hwnds = ConcurrentHashMap.newKeySet();
    private final ObjectProperty<State> state;
    private final BooleanProperty foreground;
    private final BooleanProperty minimized;

    public InstanceEntity(AppEntity app, Process pid) {
        this.app = app;
        this.process = pid;
        this.state = new SimpleObjectProperty<>(State.STARTING);
        this.foreground = new SimpleBooleanProperty(false);
        this.minimized = new SimpleBooleanProperty(false);
    }

    public AppEntity getApp() {
        return app;
    }

    public Process getProcess() {
        return process;
    }

    public Set<Long> getHwnds() {
        return hwnds;
    }

    public State getState() {
        return state.get();
    }

    public void setState(State state) {
        this.state.set(state);
    }

    public void setForeground(boolean foreground) {
        this.foreground.set(foreground);
    }

    public void setMinimized(boolean minimized) {
        this.minimized.set(minimized);
    }

    public boolean isForeground() {
        return foreground.get();
    }

    public BooleanProperty foregroundProperty() {
        return foreground;
    }

    public boolean isMinimized() {
        return minimized.get();
    }

    public BooleanProperty minimizedProperty() {
        return minimized;
    }

    public ObjectProperty<State> stateProperty() {
        return state;
    }
}
