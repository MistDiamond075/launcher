package org.launcher.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.launcher.exception.EntityValidationException;

import java.util.Objects;

public class AdminEntity implements BaseEntity {
    private final String password;
    private final Integer sessionTimeout;

    @JsonCreator
    public AdminEntity(
            @JsonProperty("password") String password,
            @JsonProperty("sessionTimeout") Integer sessionTimeout) throws EntityValidationException {
        this.password = password;
        this.sessionTimeout = sessionTimeout;
        validate();
    }

    public String getPassword() {
        return password;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AdminEntity that = (AdminEntity) o;
        return sessionTimeout == that.sessionTimeout && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(password, sessionTimeout);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "sessionTimeout=" + sessionTimeout +
                '}';
    }

    @Override
    public void validate() throws EntityValidationException {
        if(password == null || password.isBlank()){
            throw new EntityValidationException("Password is empty");
        }
    }
}
