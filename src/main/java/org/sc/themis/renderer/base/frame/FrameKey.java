package org.sc.themis.renderer.base.frame;

import org.sc.themis.core.LifeCycle;

public interface FrameKey<D extends LifeCycle> {

  static <T extends LifeCycle> FrameKey<T> of(Class<T> clazz) {
    return () -> clazz;
  }

  Class<D> getType();
}
