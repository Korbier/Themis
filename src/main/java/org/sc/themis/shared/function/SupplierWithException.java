package org.sc.themis.shared.function;

import org.sc.themis.shared.exception.ThemisException;

@FunctionalInterface
public interface SupplierWithException<T> {
    T get() throws ThemisException;
}
