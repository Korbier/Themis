package org.sc.themis.scene.base;

import org.sc.themis.input.Input;

public interface Controller {

  void update(long tpf);

  void input(Input input, long tpf);
}
