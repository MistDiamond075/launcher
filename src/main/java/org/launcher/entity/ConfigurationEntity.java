package org.launcher.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.launcher.exception.EntityValidationException;

import java.util.Objects;
import java.util.Set;

public class ConfigurationEntity implements BaseEntity {
    private final Set<AppEntity> apps;
    private final Set<FolderEntity> folders;
    private final  LauncherEntity launcher;
    private final LoggingEntity logging;
    private final AdminEntity admin;

    @JsonCreator
    public ConfigurationEntity(@JsonProperty("apps") Set<AppEntity> apps, @JsonProperty("folders") Set<FolderEntity> folders, @JsonProperty("launcher")  LauncherEntity launcher, @JsonProperty("logging")  LoggingEntity logging, @JsonProperty("admin")  AdminEntity admin) {
        this.apps = apps;
        this.folders = folders;
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

    @JsonProperty("logging")
    public LoggingEntity getLog() {
        return logging;
    }

    public AdminEntity getAdmin() {
        return admin;
    }

    public Set<FolderEntity> getFolders() {
        return folders;
    }

    @Override
    public void validate() throws EntityValidationException {
        for (AppEntity app : apps) {
            app.validate();
            folders.stream().filter(f -> f.getAppsIds().contains(app.getId())).forEach(f -> f.addApp(app));
        }
        admin.validate();
        logging.validate();
        launcher.validate();
        for (FolderEntity folder : folders) {
            folder.validate();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationEntity that = (ConfigurationEntity) o;
        return Objects.equals(apps, that.apps) && Objects.equals(folders, that.folders) && Objects.equals(launcher, that.launcher) && Objects.equals(logging, that.logging) && Objects.equals(admin, that.admin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apps, folders, launcher, logging, admin);
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
