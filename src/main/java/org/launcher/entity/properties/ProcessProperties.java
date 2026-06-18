package org.launcher.entity.properties;

import javafx.beans.property.*;

public class ProcessProperties {
    public enum States{RUNNING, STOPPED}
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty state = new SimpleStringProperty();
    private final LongProperty processId = new SimpleLongProperty();

    public ProcessProperties(String id,States state,long processId) {
        this.id.set(id);
        this.state.set(String.valueOf(state));
        this.processId.set(processId);
    }

    public String getState() {
        return state.get();
    }

    public StringProperty stateProperty() {
        return state;
    }

    public long getProcessId() {
        return processId.get();
    }

    public LongProperty processIdProperty() {
        return processId;
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setState(States state) {
        this.state.set(String.valueOf(state));
    }
}
