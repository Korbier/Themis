package org.sc.viewer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.sc.themis.input.Input;

public class ViewerKeyMapping {

  private Map<Integer, Runnable> actions = new HashMap<>();
  private Map<Integer, Boolean> repeat = new HashMap<>();

  private final Map<Integer, Supplier<?>> stateSuppliers = new HashMap<>();
  private final Map<Integer, Object> states = new HashMap<>();

  public void map(Integer key, boolean repeat, Runnable runnable) {
    map(key, repeat, runnable, null);
  }

  public <O> void map(Integer key, boolean repeat, Runnable runnable, Supplier<O> stateSupplier) {

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

  public <T> T getState(int key) {
      return (T) this.states.get(key);
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

    //Mise à jour de l'état lié a cette clé clavier
    if (this.stateSuppliers.containsKey(key)) {
      Supplier<?> supplier = this.stateSuppliers.get(key);
      this.states.put(key, supplier.get());
    }

  }

}
