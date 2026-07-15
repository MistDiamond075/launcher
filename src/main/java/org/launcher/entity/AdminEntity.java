package org.launcher.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.launcher.exception.EntityValidationException;
import org.launcher.utils.constants.KeyboardEventConstants;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AdminEntity implements BaseEntity {
    private String password;
    private final Integer sessionTimeout;
    private String strCombination;
    @JsonIgnore
    private final Set<Integer> combination;

    @JsonCreator
    public AdminEntity(
            @JsonProperty("password") String password,
            @JsonProperty("sessionTimeout") Integer sessionTimeout,
            @JsonProperty("keyCombination")String keyCombination) {
        this.password = password;
        this.sessionTimeout = sessionTimeout;
        this.combination = parseHotkey(keyCombination);
        this.strCombination = keyCombination;
    }

    public String getPassword() {
        return password;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public Set<Integer> getCombination() {
        return combination;
    }

    @JsonProperty("keyCombination")
    public String getStrCombination() {
        return strCombination;
    }

    public void setStrCombination(String strCombination) {
        this.strCombination = strCombination;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AdminEntity that = (AdminEntity) o;
        return Objects.equals(sessionTimeout, that.sessionTimeout) && Objects.equals(password, that.password);
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

    private Set<Integer> parseHotkey(String value) {
        Set<Integer> result = new HashSet<>();

        for (String token : value.split("\\+")) {
            token = token.trim().toUpperCase();

            Integer vk = KeyboardEventConstants.STRKEYS_INTKEYS.get(token);
            if (vk != null) {
                result.add(vk);
                continue;
            }

            if (token.length() == 1) {
                char ch = token.charAt(0);

                if ('A' <= ch && ch <= 'Z') {
                    result.add((int) ch);
                    continue;
                }

                if ('0' <= ch && ch <= '9') {
                    result.add((int) ch);
                    continue;
                }
            }

            if (token.matches("F([1-9]|1[0-9]|2[0-4])")) {
                int n = Integer.parseInt(token.substring(1));
                result.add(0x70 + n - 1);
                continue;
            }

            throw new IllegalArgumentException(
                    "Unknown key: " + token
            );
        }

        return Set.copyOf(result);
    }
}
