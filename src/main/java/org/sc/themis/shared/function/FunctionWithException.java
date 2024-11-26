package org.sc.themis.shared.function;

import org.sc.themis.shared.exception.ThemisException;

@FunctionalInterface
public interface FunctionWithException<T, R> {
    R apply(T t) throws ThemisException;
}
