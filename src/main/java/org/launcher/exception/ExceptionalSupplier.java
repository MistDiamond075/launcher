package org.launcher.exception;

@FunctionalInterface
public interface ExceptionalSupplier<T> {
    T get() throws Exception;
}
