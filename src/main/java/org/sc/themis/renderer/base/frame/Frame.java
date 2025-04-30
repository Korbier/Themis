package org.sc.themis.renderer.base.frame;

import java.util.HashMap;
import java.util.Map;

import org.sc.themis.core.LifeCycle;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiConsumerWithException;
import org.sc.themis.shared.function.ConsumerWithException;
import org.sc.themis.shared.function.FunctionWithException;
import org.sc.themis.shared.function.SupplierWithException;

public class Frame {

  private final Map<FrameKey<? extends LifeCycle>, LifeCycle> content = new HashMap<>();

  private final boolean autoSetup;
  private final boolean autoCleanup;

  public Frame(boolean autoSetup, boolean autoCleanup) {
    this.autoSetup = autoSetup;
    this.autoCleanup = autoCleanup;
  }

  public <T extends LifeCycle> T get(FrameKey<T> key) {
    return (T) this.content.get(key);
  }

  public void cleanup() throws ThemisException {
    for (LifeCycle o : this.content.values()) {
      o.cleanup();
    }
  }

  <T extends LifeCycle> T create(FrameKey<T> key, SupplierWithException<T> supplier)
      throws ThemisException {
    return put(key, supplier.get());
  }

  <T extends LifeCycle> T create(
      FrameKey<T> key, int frame, FunctionWithException<Integer, T> function)
      throws ThemisException {
    return put(key, function.apply(frame));
  }

  <E extends ThemisException, T extends LifeCycle> T update(FrameKey<T> key, ConsumerWithException<E, T> consumer) throws E {
    T data = (T) this.content.get(key);
    consumer.accept(data);
    return data;
  }

  <E extends ThemisException, T extends LifeCycle> T update(FrameKey<T> key, int frame, BiConsumerWithException<E, Integer, T> consumer) throws E {
    T data = (T) this.content.get(key);
    consumer.accept(frame, data);
    return data;
  }

  <T extends LifeCycle> T remove(FrameKey<T> key) throws ThemisException {
    T data = (T) this.content.remove(key);
    if (this.autoCleanup) data.cleanup();
    return data;
  }

  private <T extends LifeCycle> T put(FrameKey<T> key, T data) throws ThemisException {
    if (this.autoSetup) data.setup();
    this.content.put(key, data);
    return data;
  }
}
