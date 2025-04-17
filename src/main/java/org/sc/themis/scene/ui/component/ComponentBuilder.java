package org.sc.themis.scene.ui.component;

import java.util.*;
import java.util.function.Consumer;

import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.renderer.pencil2d.Pencil2DLayer;
import org.sc.themis.renderer.resource.font.FontRepository;
import org.sc.themis.scene.ui.ComponentState;
import org.sc.themis.scene.ui.UiBuilder;
import org.sc.themis.scene.ui.UiState;

public abstract sealed class ComponentBuilder<B extends ComponentBuilder<?>>
    permits ButtonBuilder, ComboboxBuilder, ContainerBuilder, LabelBuilder, PanelBuilder, ToggleButtonBuilder {

  private final UiBuilder uiBuilder;
  private final Map<String, Consumer<UiBuilder>> events = new HashMap<>();
  private final List<ComponentBuilder<?>> children = new ArrayList<>();

  public static final ComponentState<String> STATE_LAST_ACTIVE = ComponentState.of(String.class, "last.active");

  protected final int[] hotRegion = new int[] {0, 0, 0, 0};
  private final int[] childrenOffsets = new int[] {0, 0};

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
    int left = parent != null ? parent.left + parent.childrenOffsets[0] + this.left : this.left;
    int top = parent != null ? parent.top + parent.childrenOffsets[1] + this.top : this.top;
    int width = this.width;
    int height = this.height;

    setHotRegion(left, top, width, height);
    configure(left, top, width, height);
    checkState();

    if (debug()) {
      pencil().color(Color.of("FF0000")).rect(left, top, width, height);
    }

    draw(left, top, width, height);

    triggerEvents();

    if (!this.children.isEmpty()) {
      state().pushParent(this);
      try {
        bringActiveChildToFront();
        for (ComponentBuilder<?> child : children) {
          child.build();
        }
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

  protected void setChildrenOffsets(int left, int top) {
    this.childrenOffsets[0] = left;
    this.childrenOffsets[1] = top;
  }

  protected void setHotRegion(int left, int top, int width, int height) {
    this.hotRegion[0] = left;
    this.hotRegion[1] = top;
    this.hotRegion[2] = width;
    this.hotRegion[3] = height;
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

  protected Pencil2DLayer pencil() {
    return this.uiBuilder.pencil();
  }

  protected boolean areFontsAvailable() {
    return pencil().isFontRepositoryAvailable();
  }

  protected FontRepository getFonts() {
    return pencil().getFont();
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

  private boolean hotRegionHit() {
    return !((state().getMouseX() < this.hotRegion[0])
        || (state().getMouseY() < this.hotRegion[1])
        || (state().getMouseX() >= (this.hotRegion[0] + this.hotRegion[2]))
        || (state().getMouseY() >= (this.hotRegion[1] + this.hotRegion[3])));
  }

  private void checkState() {

    if (hotRegionHit()) {

      state().setHotItem(this.identifier);

      boolean canBeActive =
          state().getActiveItem() == null
          || (getParent() != null && state().getActiveItem().equals(getParent().identifier));

      if (canBeActive && state().isMouseDown()) {

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
