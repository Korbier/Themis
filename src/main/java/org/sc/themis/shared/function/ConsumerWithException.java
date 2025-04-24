package org.sc.themis.shared.function;

import org.sc.themis.shared.exception.ThemisException;

@FunctionalInterface
public interface ConsumerWithException<E extends ThemisException, IN> {
  void accept(IN input) throws E;
}
