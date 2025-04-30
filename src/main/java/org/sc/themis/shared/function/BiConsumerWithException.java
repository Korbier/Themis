package org.sc.themis.shared.function;

import org.sc.themis.shared.exception.ThemisException;

@FunctionalInterface
public interface BiConsumerWithException<E extends ThemisException, T, U> {
  void accept(T t, U u) throws E;
}
