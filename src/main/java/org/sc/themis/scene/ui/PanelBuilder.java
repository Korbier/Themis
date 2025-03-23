package org.sc.themis.scene.ui;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.sc.themis.scene.pencil.Color;

public final class PanelBuilder extends ComponentBuilder<PanelBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;
  public static final int DEFAULT_HEADER_SIZE = 24;

  public static final ComponentState<Vector2i> STATE_MOUSE_POSITION =
      ComponentState.of(Vector2i.class, "mouse.position");

  PanelBuilder(UIBuilder builder) {
    super(builder);
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
      location(
          left + (newpos.x - oldpos.x),
          top + (newpos.y - oldpos.y)
      );
      set(STATE_MOUSE_POSITION, new Vector2i(state().getMouseX(), state().getMouseY()));
    }

    setRegion(
        left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE,
        width - 2 * DEFAULT_BORDER_SIZE, DEFAULT_HEADER_SIZE
    );

  }

  @Override
  protected void draw(int left, int top, int width, int height) {

    Color borderColor = Color.of("555555");
    Color backgroundColor = Color.of("222222"); //Color.of("eeeeee");
    Color headerColor = Color.of("555555"); //Color.of("CBD5E1");
    Color headerColorHot = Color.of("777777");

    pencil().rect(new Vector2f(left, top), new Vector2f(width, height), borderColor);
    pencil().rect(
        new Vector2f(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE),
        new Vector2f(width - 2 * DEFAULT_BORDER_SIZE, height - 2 * DEFAULT_BORDER_SIZE),
        backgroundColor
    );

    if (isHotItem()) {
      pencil().rect(
          new Vector2f(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE),
          new Vector2f(width - 2 * DEFAULT_BORDER_SIZE, DEFAULT_HEADER_SIZE),
          headerColorHot
      );


    } else {
      pencil().rect(
          new Vector2f(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE),
          new Vector2f(width - 2 * DEFAULT_BORDER_SIZE, DEFAULT_HEADER_SIZE),
          headerColor
      );
    }

    pencil().text(
        new Vector2f(left + 2 * DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE * 2),
        0, Color.of("FFFFFF"), "Configuration"
    );

  }

  @Override
  protected void triggerEvents() {
  }

}
