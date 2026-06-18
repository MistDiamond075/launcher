package org.launcher.service;

import java.util.Optional;
import java.util.Set;

public class ServiceKeys {
    private final Set<String> keys;

    public ServiceKeys(Set<String> keys) {
        this.keys = keys;
    }

    public String getKey(String key) {
        Optional<String> found = keys.stream().filter(key::equals).findFirst();
        return found.orElse(null);
    }
}
