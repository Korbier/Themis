package org.sc.themis.scene.ui.component;

import java.util.*;
import java.util.function.Consumer;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.renderer.pencil2d.Pencil2D;
import org.sc.themis.renderer.pencil2d.Pencil2DLayer;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.scene.ui.ComponentState;
import org.sc.themis.scene.ui.UiBuilder;
import org.sc.themis.scene.ui.UiState;

public abstract sealed class ComponentBuilder<B extends ComponentBuilder<?>>
    permits ButtonBuilder, ComboboxBuilder, ContainerBuilder, LabelBuilder, PanelBuilder, ToggleButtonBuilder {

  private final UiBuilder uiBuilder;
  private final Map<String, Consumer<UiBuilder>> events = new HashMap<>();
  private final List<ComponentBuilder<?>> children = new ArrayList<>();

  public static final ComponentState<String> STATE_LAST_ACTIVE = ComponentState.of(String.class, "last.active");

  private final int[] pushedRegion = new int[] {0, 0, 0, 0};
  private final int[] region = new int[] {0, 0, 0, 0};

  private String identifier;
  private int left = 0;
  private int top = 0;
  private int width = 0;
  private int height = 0;

  private boolean debug = false;

  protected ComponentBuilder(UiBuilder uiBuilder) {
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
    pushRegion();

    configure(left, top, width, height);
    checkState();

    if (debug()) {
      background().rect(left, top, width, height);
    }

    draw(left, top, width, height);
    triggerEvents();

    if (!this.children.isEmpty()) {
      state().pushParent(this);
      try {
        bringActiveChildToFront();
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

  public B debug(boolean debug) {
    this.debug = debug;
    return (B) this;
  }

  public B position(int left, int top) {
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

  public boolean debug() {
    return this.debug;
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

  protected void pushRegion() {
    this.pushedRegion[0] = this.region[0];
    this.pushedRegion[1] = this.region[1];
    this.pushedRegion[2] = this.region[2];
    this.pushedRegion[3] = this.region[3];
  }

  protected void popRegion() {
    this.region[0] = this.pushedRegion[0];
    this.region[1] = this.pushedRegion[1];
    this.region[2] = this.pushedRegion[2];
    this.region[3] = this.pushedRegion[3];
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

  protected Pencil2DLayer background() {
    return this.uiBuilder.background();
  }

  protected Pencil2DLayer foreground() {
    return this.uiBuilder.foreground();
  }

  protected void addEvent(String event, Consumer<UiBuilder> eventConsumer) {
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
        if (getParent() != null) {
          getParent().set(STATE_LAST_ACTIVE, this.identifier);
        }
      }

    }

  }

  private String getActiveChild() {
    return this.children.stream().filter(ComponentBuilder::isActiveItem).map(ComponentBuilder::identifier).findFirst().orElse(null);
  }

  private void bringActiveChildToFront() {

    String activeIdentifier = getActiveChild();

    if (activeIdentifier == null) {
      activeIdentifier = get(STATE_LAST_ACTIVE);
    }

    if (activeIdentifier != null) {
      int idx = -1;
      for (int i = 0; i < this.children.size(); i++) {
        if (this.children.get(i).identifier().equals(activeIdentifier)) {
          idx = i;
        }
      }
      if (idx > -1) {
        ComponentBuilder<?> cmp = this.children.get(idx);
        this.children.remove(idx);
        this.children.add(cmp);
      }
    }

  }

}
