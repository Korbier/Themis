package org.sc.themis.shared.function;

import org.sc.themis.shared.exception.ThemisException;

@FunctionalInterface
public interface BiFunctionWithException<A, B, R> {
  R apply(A a, B b) throws ThemisException;
}
