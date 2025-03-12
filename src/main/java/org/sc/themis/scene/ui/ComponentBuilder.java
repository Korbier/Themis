package org.sc.themis.scene.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.sc.themis.scene.pencil.Pencil;

public abstract sealed class ComponentBuilder<B extends ComponentBuilder<?>>
        permits
          ButtonBuilder,
          ToggleButtonBuilder,
          PanelBuilder
{

  private final UIBuilder uiBuilder;
  private final Map<String, Consumer<UIBuilder>> events = new HashMap<>();

  private String identifier;
  private int left = 0;
  private int top = 0;
  private int width = 0;
  private int height = 0;

  private int[] region = new int[] {0, 0, 0, 0};

  protected ComponentBuilder(UIBuilder uiBuilder) {
    this.uiBuilder = uiBuilder;
  }

  protected abstract void checkInput();
  protected abstract void draw();
  protected abstract void triggerEvents();

  public void build() {
    checkInput();
    checkState();
    draw();
    triggerEvents();
  }

  public String identifier() {
    return this.identifier;
  }

  public int left() {
    return this.left;
  }

  public int top() {
    return this.top;
  }

  public int width() {
    return this.width;
  }

  public int height() {
    return this.height;
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
    this.region[0] = left;
    this.region[1] = top;
    return (B) this;
  }


  public B size(int width, int height) {
    this.width = width;
    this.height = height;
    this.region[2] = width;
    this.region[3] = height;
    return (B) this;
  }

  protected void setRegion(int left, int top, int width, int height) {
    this.region[0] = left;
    this.region[1] = top;
    this.region[2] = width;
    this.region[3] = height;
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
