package org.sc.themis.scene.ui;

import java.util.Objects;

public class ComponentState<T> {

  private Class<T> type;
  private String name;

  public static <O> ComponentState<O> of(Class<O> type, String name) {
    ComponentState<O> state = new ComponentState<>();
    state.type = type;
    state.name = name;
    return state;
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ComponentState<?> that = (ComponentState<?>) o;
    return Objects.equals(name, that.name);

  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

}