package org.launcher.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class LauncherEntity implements BaseEntity{
    private static final Logger logger = LoggerFactory.getLogger(LauncherEntity.class);
    private final String title;
    private final String orgName;
    private final boolean fullscreen;
    private final boolean showDate;
    private final boolean hideCursor;
    private final boolean disableBackgroundAnimation;
    private final boolean allowMultipleInstances;
    private final boolean adminMenuEnabled;
    @JsonIgnore
    private final boolean isTitlePicture;

    @JsonCreator
    public LauncherEntity(@JsonProperty("title") String title,
                          @JsonProperty("orgName")  String orgName,
                          @JsonProperty("fullscreen") boolean fullscreen,
                          @JsonProperty("showDate") boolean showDate,
                          @JsonProperty("hideCursor") boolean hideCursor,
                          @JsonProperty("disableBackgroundAnimation")boolean disableBackgroundAnimation,
                          @JsonProperty("allowMultipleInstances") boolean allowMultipleInstances,
                          @JsonProperty("adminMenuEnabled") boolean adminMenuEnabled) {
        this.title = title;
        this.orgName = orgName;
        this.fullscreen = fullscreen;
        this.showDate = showDate;
        this.hideCursor = hideCursor;
        this.disableBackgroundAnimation = disableBackgroundAnimation;
        this.allowMultipleInstances = allowMultipleInstances;
        this.adminMenuEnabled = adminMenuEnabled;
        boolean isPic = false;
        try{
            Path pathToTitle = Paths.get(title);
            isPic = Files.exists(pathToTitle);
        } catch (Exception ignored) {
        }finally{
            isTitlePicture = isPic;
        }
    }

    public String getTitle() {
        return title;
    }

    public boolean isTitlePicture() {
        return isTitlePicture;
    }

    public boolean isDisableBackgroundAnimation() {
        return disableBackgroundAnimation;
    }

    public boolean isAllowMultipleInstances() {
        return allowMultipleInstances;
    }

    public boolean isAdminMenuEnabled() {
        return adminMenuEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LauncherEntity that = (LauncherEntity) o;
        return Objects.equals(title, that.title) && Objects.equals(orgName, that.orgName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, orgName);
    }

    @Override
    public String toString() {
        return "LauncherEntity{" +
                "title='" + title + '\'' +
                ", orgName='" + orgName + '\'' +
                ", fullscreen=" + fullscreen +
                ", showDate=" + showDate +
                ", hideCursor=" + hideCursor +
                ", allowMultipleInstances=" + allowMultipleInstances +
                ", adminMenuEnabled=" + adminMenuEnabled +
                '}';
    }

    @Override
    public void validate() {

    }
}
