package org.launcher.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class LauncherEntity implements BaseEntity{
    private final String title;
    private final String orgName;
    private final boolean fullscreen;
    private final boolean showDate;
    private final boolean showVersion;
    private final boolean hideCursor;
    private final boolean allowMultipleInstances;
    private final boolean adminMenuEnabled;

    @JsonCreator
    public LauncherEntity(@JsonProperty("title") String title,
                          @JsonProperty("orgName")  String orgName,
                          @JsonProperty("fullscreen") boolean fullscreen,
                          @JsonProperty("showDate") boolean showDate,
                          @JsonProperty("showVersion") boolean showVersion,
                          @JsonProperty("hideCursor") boolean hideCursor,
                          @JsonProperty("allowMultipleInstances") boolean allowMultipleInstances,
                          @JsonProperty("adminMenuEnabled") boolean adminMenuEnabled) {
        this.title = title;
        this.orgName = orgName;
        this.fullscreen = fullscreen;
        this.showDate = showDate;
        this.showVersion = showVersion;
        this.hideCursor = hideCursor;
        this.allowMultipleInstances = allowMultipleInstances;
        this.adminMenuEnabled = adminMenuEnabled;
    }

    public String getTitle() {
        return title;
    }

    public String getOrgName() {
        return orgName;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public boolean isShowDate() {
        return showDate;
    }

    public boolean isShowVersion() {
        return showVersion;
    }

    public boolean isHideCursor() {
        return hideCursor;
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
                ", showVersion=" + showVersion +
                ", hideCursor=" + hideCursor +
                ", allowMultipleInstances=" + allowMultipleInstances +
                ", adminMenuEnabled=" + adminMenuEnabled +
                '}';
    }

    @Override
    public void validate() {
    }
}
