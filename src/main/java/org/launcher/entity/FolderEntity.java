package org.launcher.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.launcher.exception.EntityValidationException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FolderEntity implements BaseEntity {
    private final String name;
    private final Set<String> appsIds;
    @JsonIgnore
    private final Set<AppEntity> apps;

    @JsonCreator
    public FolderEntity(@JsonProperty("name") String name, @JsonProperty("apps") Set<String> apps) {
        this.name = name;
        this.appsIds = apps;
        this.apps = new HashSet<>();
    }

    public void addApp(AppEntity app) {
        this.apps.add(app);
    }

    public Set<AppEntity> getApps(){
        return apps;
    }

    public Set<String> getAppsIds(){
        return appsIds;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FolderEntity that = (FolderEntity) o;
        return Objects.equals(appsIds, that.appsIds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(appsIds);
    }

    @Override
    public void validate() throws EntityValidationException {
    }
}
