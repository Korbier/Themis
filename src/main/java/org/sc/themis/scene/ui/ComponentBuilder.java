package org.sc.themis.scene.ui;

import java.util.*;
import java.util.function.Consumer;
import org.sc.themis.scene.pencil.Pencil;

public abstract sealed class ComponentBuilder<B extends ComponentBuilder<?>>
        permits
          ButtonBuilder,
          ToggleButtonBuilder,
          PanelBuilder,
          LabelBuilder {

  private final UIBuilder uiBuilder;
  private final Map<String, Consumer<UIBuilder>> events = new HashMap<>();
  private final List<ComponentBuilder<?>> children = new ArrayList<>();

  private final int[] region = new int[] {0, 0, 0, 0};

  private String identifier;
  private int left = 0;
  private int top = 0;
  private int width = 0;
  private int height = 0;

  protected ComponentBuilder(UIBuilder uiBuilder) {
    this.uiBuilder = uiBuilder;
  }

  protected abstract void configure(int left, int top, int width, int height);
  protected abstract void draw(int left, int top, int width, int height);
  protected abstract void triggerEvents();

  public void build() {

    ComponentBuilder<?> parent = getParent();
    int left = parent != null ? parent.left + this.left : this.left;
    int top = parent != null ? parent.top + this.top : this.top;
    int width = this.width;
    int height = this.height;

    setRegion(left, top, width, height);

    configure(left, top, width, height);
    checkState();

    draw(left, top, width, height);
    triggerEvents();

    if (!this.children.isEmpty()) {
      state().pushParent(this);
      try {
        this.children.forEach(ComponentBuilder::build);
      } finally {
        state().popParent();
      }
    }

  }

  public String identifier() {
    return this.identifier;
  }

  public boolean isHotItem() {
    return identifier().equals(state().getHotItem());
  }

  public boolean isActiveItem() {
    return identifier().equals(state().getActiveItem());
  }

  public B identifier(String identifier) {
    this.identifier = identifier;
    return (B) this;
  }

  public B location(int left, int top) {
    this.left = left;
    this.top = top;
    return (B) this;
  }

  public B size(int width, int height) {
    this.width = width;
    this.height = height;
    return (B) this;
  }

  public B child(ComponentBuilder<?> ... children) {
    this.children.addAll(Arrays.asList(children));
    return (B) this;
  }

  protected ComponentBuilder<?> getParent() {
    return state().getParent();
  }

  protected void setRegion(int left, int top, int width, int height) {
    this.region[0] = left;
    this.region[1] = top;
    this.region[2] = width;
    this.region[3] = height;
  }

  protected <T> void set(ComponentState<T> state, T value) {
    state().setComponentState(this, state, value);
  }

  protected <T> T get(ComponentState<T> state) {
    return state().getComponentState(this, state);
  }

  protected <T> boolean contains(ComponentState<T> state) {
    return state().containsComponentState(this, state);
  }

  protected <T> void remove(ComponentState<T> state) {
    state().removeComponentState(this, state);
  }

  protected UiState state() {
    return this.uiBuilder.getState();
  }

  protected Pencil pencil() {
    return this.uiBuilder.getPencil();
  }

  protected void addEvent(String event, Consumer<UIBuilder> eventConsumer) {
    this.events.put(event, eventConsumer);
  }

  protected void fireEvent(String event) {
    if (isEventDefined(event)) {
      this.events.get(event).accept(this.uiBuilder);
    }
  }

  protected boolean isEventDefined(String event) {
    return this.events.containsKey(event);
  }

  private boolean regionHit() {
    return !((state().getMouseX() < this.region[0])
        || (state().getMouseY() < this.region[1])
        || (state().getMouseX() >= (this.region[0] + this.region[2]))
        || (state().getMouseY() >= (this.region[1] + this.region[3])));
  }

  private void checkState() {

    if (regionHit()) {

      state().setHotItem(this.identifier);

      if (state().getActiveItem() == null && state().isMouseDown()) {
        state().setActiveItem(this.identifier);
      }

    }

  }

}
