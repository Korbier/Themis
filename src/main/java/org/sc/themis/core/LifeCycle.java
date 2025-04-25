package org.sc.themis.core;

import org.sc.themis.shared.exception.ThemisException;

public interface LifeCycle {
  void setup() throws ThemisException;
  void cleanup() throws ThemisException;
}
