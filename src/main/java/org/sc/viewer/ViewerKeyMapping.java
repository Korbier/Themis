package org.sc.viewer;

import java.util.HashMap;
import java.util.Map;
import org.sc.themis.input.Input;

public class ViewerKeyMapping {

  private Map<Integer, Runnable> actions = new HashMap<>();
  private Map<Integer, Boolean> repeat = new HashMap<>();

  public void map(Integer key, boolean repeat, Runnable runnable) {
    this.actions.put(key, runnable);
    this.repeat.put(key, repeat);
  }

  public void execute(int key) {
    if (this.actions.containsKey(key)) {
      this.actions.get(key).run();
    }
  }

  public void input(Input input) {

    for (int key : this.actions.keySet()) {

      boolean pressed;

      if (this.repeat.get(key)) {
        pressed = input.isKeyPressed(key);
      } else {
        pressed = input.isKeyPressedNoRepeat(key);
      }

      if (pressed) {
        this.actions.get(key).run();
      }
    }
  }
}
