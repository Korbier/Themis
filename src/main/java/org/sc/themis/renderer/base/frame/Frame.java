package org.sc.themis.renderer.base.frame;

import java.util.HashMap;
import java.util.Map;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiConsumerWithException;
import org.sc.themis.shared.function.ConsumerWithException;
import org.sc.themis.shared.function.FunctionWithException;
import org.sc.themis.shared.function.SupplierWithException;
import org.sc.themis.shared.tobject.TObject;

public class Frame {

  private final Map<FrameKey<? extends TObject>, TObject> content = new HashMap<>();

  private final boolean autoSetup;
  private final boolean autoCleanup;

  public Frame(boolean autoSetup, boolean autoCleanup) {
    this.autoSetup = autoSetup;
    this.autoCleanup = autoCleanup;
  }

  public <T extends TObject> T get(FrameKey<T> key) {
    return (T) this.content.get(key);
  }

  public void cleanup() throws ThemisException {
    for (TObject o : this.content.values()) {
      o.cleanup();
    }
  }

  <T extends TObject> T create(FrameKey<T> key, SupplierWithException<T> supplier)
      throws ThemisException {
    return put(key, supplier.get());
  }

  <T extends TObject> T create(
      FrameKey<T> key, int frame, FunctionWithException<Integer, T> function)
      throws ThemisException {
    return put(key, function.apply(frame));
  }

  <T extends TObject> T update(FrameKey<T> key, ConsumerWithException<T> consumer)
      throws ThemisException {
    T data = (T) this.content.get(key);
    consumer.accept(data);
    return data;
  }

  <T extends TObject> T update(
      FrameKey<T> key, int frame, BiConsumerWithException<Integer, T> consumer)
      throws ThemisException {
    T data = (T) this.content.get(key);
    consumer.accept(frame, data);
    return data;
  }

  <T extends TObject> T remove(FrameKey<T> key) throws ThemisException {
    T data = (T) this.content.remove(key);
    if (this.autoCleanup) data.cleanup();
    return data;
  }

  private <T extends TObject> T put(FrameKey<T> key, T data) throws ThemisException {
    if (this.autoSetup) data.setup();
    this.content.put(key, data);
    return data;
  }
}
