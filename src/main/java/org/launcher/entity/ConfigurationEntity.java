package org.launcher.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

public class ConfigurationEntity {
    private final Set<AppEntity> apps;
    private final  LauncherEntity launcher;
    private final LoggingEntity logging;
    private final AdminEntity admin;

    @JsonCreator
    public ConfigurationEntity(@JsonProperty("apps") Set<AppEntity> apps,@JsonProperty("launcher")  LauncherEntity launcher,@JsonProperty("logging")  LoggingEntity logging,@JsonProperty("admin")  AdminEntity admin) {
        this.apps = apps;
        this.launcher = launcher;
        this.logging = logging;
        this.admin = admin;
    }

    public Set<AppEntity> getApps() {
        return apps;
    }

    public LauncherEntity getLauncher() {
        return launcher;
    }

    public LoggingEntity getLog() {
        return logging;
    }

    public AdminEntity getAdmin() {
        return admin;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationEntity that = (ConfigurationEntity) o;
        return Objects.equals(apps, that.apps) && Objects.equals(launcher, that.launcher) && Objects.equals(logging, that.logging) && Objects.equals(admin, that.admin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apps, launcher, logging, admin);
    }

    @Override
    public String toString() {
        return "ConfigurationEntity{" +
                "apps=" + apps +
                ", launcher=" + launcher +
                ", log=" + logging +
                ", admin=" + admin +
                '}';
    }
}
