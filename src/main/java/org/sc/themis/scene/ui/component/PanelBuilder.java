package org.sc.themis.scene.ui.component;

import org.joml.Vector2i;
import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.scene.ui.ComponentState;
import org.sc.themis.scene.ui.UiBuilder;

public final class PanelBuilder extends ComponentBuilder<PanelBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;
  public static final int DEFAULT_HEADER_SIZE = 20;

  private LabelBuilder lblTitle = null;
  private Color color = Color.of("555555");
  private Color hotColor = Color.of("777777");
  private Color backgroundColor = Color.of("222222");

  public static final ComponentState<Vector2i> STATE_MOUSE_POSITION = ComponentState.of(Vector2i.class, "mouse.position");

  public PanelBuilder(UiBuilder builder) {
    super(builder);
    this.lblTitle = builder.label();
    setChildrenOffsets(DEFAULT_BORDER_SIZE, DEFAULT_BORDER_SIZE + DEFAULT_HEADER_SIZE);
  }

  public PanelBuilder text(String text) {
    this.lblTitle.text(text);
    return this;
  }

  public PanelBuilder font(int index) {
    this.lblTitle.font(index);
    return this;
  }

  public PanelBuilder color(Color color) {
    this.color = color;
    return this;
  }

  public PanelBuilder hotColor(Color color) {
    this.hotColor = color;
    return this;
  }

  public PanelBuilder backgroundColor(Color color) {
    this.backgroundColor = color;
    return this;
  }

  @Override
  protected void configure(int left, int top, int width, int height) {

    if (isActiveItem()) {
      if (!contains(STATE_MOUSE_POSITION)) {
        set(STATE_MOUSE_POSITION, new Vector2i(state().getMouseX(), state().getMouseY()));
      }
    } else {
      if (contains(STATE_MOUSE_POSITION)) {
        remove(STATE_MOUSE_POSITION);
      }
    }

    if (contains(STATE_MOUSE_POSITION)) {
      Vector2i oldpos = get(STATE_MOUSE_POSITION);
      Vector2i newpos = new Vector2i(state().getMouseX(), state().getMouseY());
      position(left + (newpos.x - oldpos.x), top + (newpos.y - oldpos.y));
      set(STATE_MOUSE_POSITION, new Vector2i(state().getMouseX(), state().getMouseY()));
    }
/*
    setRegion(
        left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE,
        width - 2 * DEFAULT_BORDER_SIZE, DEFAULT_HEADER_SIZE
    );
*/
  }

  @Override
  protected void draw(int left, int top, int width, int height) {

    pencil().reset();

    pencil()
        .color(isHotItem() ? this.hotColor : this.color)
        .rect(left, top, width, height)
        .color(this.backgroundColor)
        .rect(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE, width - 2 * DEFAULT_BORDER_SIZE, height - 2 * DEFAULT_BORDER_SIZE)
        .color(isHotItem() ? this.hotColor : this.color)
        .rect(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE, width - 2 * DEFAULT_BORDER_SIZE, DEFAULT_HEADER_SIZE);

    this.lblTitle.draw(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE, width - 2 * DEFAULT_BORDER_SIZE, DEFAULT_HEADER_SIZE);

  }

  @Override
  protected void triggerEvents() {
  }

}
