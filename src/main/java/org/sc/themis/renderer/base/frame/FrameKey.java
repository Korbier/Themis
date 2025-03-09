package org.sc.themis.renderer.base.frame;

import org.sc.themis.shared.tobject.TObject;

public interface FrameKey<D extends TObject> {

  static <T extends TObject> FrameKey<T> of(Class<T> clazz) {
    return () -> clazz;
  }

  Class<D> getType();
}
