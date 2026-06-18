package org.launcher.entity;

import org.launcher.exception.EntityValidationException;

public interface BaseEntity {
    void validate() throws EntityValidationException;
}
