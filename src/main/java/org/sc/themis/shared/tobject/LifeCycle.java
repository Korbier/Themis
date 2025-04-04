package org.sc.themis.shared.tobject;

import org.sc.themis.shared.exception.ThemisException;

public interface LifeCycle {
  void setup() throws ThemisException;
  void cleanup() throws ThemisException;
}
