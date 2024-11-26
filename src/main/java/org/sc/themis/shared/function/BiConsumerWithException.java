package org.sc.themis.shared.function;

import org.sc.themis.shared.exception.ThemisException;

@FunctionalInterface
public interface BiConsumerWithException<T, U> {
    void accept(T t, U u) throws ThemisException;
}