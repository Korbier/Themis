package org.sc.viewer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.sc.themis.input.Input;

public class ViewerKeyMapping {

  private Map<Integer, Runnable> actions = new HashMap<>();
  private Map<Integer, Boolean> repeat = new HashMap<>();

  //todo replace Object with a generic type
  private Map<Integer, Supplier<Object>> stateSuppliers = new HashMap<>();
  private Map<Integer, Object> states = new HashMap<>();

  public void map(Integer key, boolean repeat, Runnable runnable) {
    map(key, repeat, runnable, null);
  }

  public void map(Integer key, boolean repeat, Runnable runnable, Supplier<Object> stateSupplier) {

    this.actions.put(key, runnable);
    this.repeat.put(key, repeat);

    if (stateSupplier != null) {
      this.stateSuppliers.put(key, stateSupplier);
      this.states.put(key, stateSupplier.get());
    }

  }

  public void execute(int key) {
    if (this.actions.containsKey(key)) {
      doExecute(key);
    }
  }

  public Object getState(int key) {
    return this.states.get(key);
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
        doExecute(key);
      }
    }
  }

  private void doExecute(int key) {
    this.actions.get(key).run();
    if (this.stateSuppliers.containsKey(key)) {
      this.states.put(key, this.stateSuppliers.get(key).get());
    }
  }
}
