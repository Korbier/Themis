package org.sc.themis.scene.ui;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class UiState {

  private int mouseX = -1;
  private int mouseY = -1;
  private boolean mouseDown = false;
  private String hotItem = null;
  private String activeItem = null;

  private Deque<ComponentBuilder<?>> parentStack = new ArrayDeque<ComponentBuilder<?>>();

  private final Map<ComponentBuilder<?>, Map<ComponentState<?>, Object>> componentStates = new HashMap<>();

  public int getMouseX() {
    return mouseX;
  }

  public void setMouseX(int mouseX) {
    this.mouseX = mouseX;
  }

  public int getMouseY() {
    return mouseY;
  }

  public void setMouseY(int mouseY) {
    this.mouseY = mouseY;
  }

  public boolean isMouseDown() {
    return mouseDown;
  }

  public void setMouseDown(boolean mouseDown) {
    this.mouseDown = mouseDown;
  }

  public String getHotItem() {
    return hotItem;
  }

  public void setHotItem(String hotItem) {
    this.hotItem = hotItem;
  }

  public String getActiveItem() {
    return activeItem;
  }

  public void setActiveItem(String activeItem) {
    this.activeItem = activeItem;
  }

  public void pushParent(ComponentBuilder<?> parent) {
    this.parentStack.push(parent);
  }

  public void popParent() {
    this.parentStack.pop();
  }

  public ComponentBuilder<?> getParent() {
    return this.parentStack.peek();
  }

  public <T> void setComponentState(ComponentBuilder<?> owner, ComponentState<T> key, T value) {

    Map<ComponentState<?>, Object> componentStates =
        this.componentStates.computeIfAbsent(owner, k -> new HashMap<>());

    componentStates.put(key, value);

  }

  public <T> T getComponentState(ComponentBuilder<?> owner, ComponentState<T> key) {

    if (!this.componentStates.containsKey(owner)) {
      return null;
    }

    if (!this.componentStates.get(owner).containsKey(key)) {
      return null;
    }

    return (T) this.componentStates.get(owner).get(key);

  }

  public <T> boolean containsComponentState(ComponentBuilder<?> owner, ComponentState<T> key) {

    if (!this.componentStates.containsKey(owner)) {
      return false;
    }

    if (!this.componentStates.get(owner).containsKey(key)) {
      return false;
    }

    return true;

  }

  public <T> void removeComponentState(ComponentBuilder<?> owner, ComponentState<T> key) {

    if (this.componentStates.containsKey(owner)) {
      this.componentStates.get(owner).remove(key);
    }

  }

}
