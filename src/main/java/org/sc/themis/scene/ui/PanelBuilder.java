package org.sc.themis.scene.ui;

import org.joml.Vector2i;
import org.sc.themis.scene.pencil.Color;

public final class PanelBuilder extends ComponentBuilder<PanelBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;
  public static final int DEFAULT_HEADER_SIZE = 24;

  private String text = null;

  private Color clrBackgroundColor = Color.of("ffffff");
  private Color clrBorderColor = Color.of("ff0000");

  private Color clrHeader = Color.of("dddddd");
  private Color clrHot = Color.of("ff0000");

  PanelBuilder(UIBuilder builder) {
    super(builder);
  }

  PanelBuilder text(String text) {
    this.text = text;
    return this;
  }

  @Override
  protected void checkInput() {

    if (isActiveItem()) {
      if (!state().contains("position" + identifier())) {
        state().set("position" + identifier(), new Vector2i(state().getMouseX(), state().getMouseY()));
      }
    } else {
      if (state().contains("position" + identifier())) {
        state().remove("position" + identifier());
      }
    }

    if (state().contains("position" + identifier())) {
      Vector2i oldpos = (Vector2i) state().get("position" + identifier());
      Vector2i newpos = new Vector2i(state().getMouseX(), state().getMouseY());
      location(
          left() + (newpos.x - oldpos.x),
          top() + (newpos.y - oldpos.y)
      );
      state().set("position" + identifier(), new Vector2i(state().getMouseX(), state().getMouseY()));
    }

    setRegion(
        this.left() + DEFAULT_BORDER_SIZE, this.top() + DEFAULT_BORDER_SIZE,
        this.width() - 2 * DEFAULT_BORDER_SIZE, DEFAULT_HEADER_SIZE
    );

  }

  @Override
  protected void draw() {

    Color borderColor = Color.of("ff0000");
    Color backgroundColor = Color.of("ffffff");
    Color headerColor = Color.of("00ff00");
    Color headerColorHot = Color.of("227722");

    pencil().drawRect(this.left(), this.top(), this.width(), this.height(), borderColor);
    pencil().drawRect(
        this.left() + DEFAULT_BORDER_SIZE, this.top() + DEFAULT_BORDER_SIZE,
        this.width() - 2 * DEFAULT_BORDER_SIZE, this.height() - 2 * DEFAULT_BORDER_SIZE,
        backgroundColor
    );

    if (isHotItem()) {
      pencil().drawRect(
          this.left() + DEFAULT_BORDER_SIZE, this.top() + DEFAULT_BORDER_SIZE,
          this.width() - 2 * DEFAULT_BORDER_SIZE, DEFAULT_HEADER_SIZE,
          headerColorHot
      );


    } else {
      pencil().drawRect(
          this.left() + DEFAULT_BORDER_SIZE, this.top() + DEFAULT_BORDER_SIZE,
          this.width() - 2 * DEFAULT_BORDER_SIZE, DEFAULT_HEADER_SIZE,
          headerColor
      );
    }

    pencil().drawText(
        this.left() + 2 * DEFAULT_BORDER_SIZE, this.top() + 2 * DEFAULT_BORDER_SIZE,
        16, "My frame"
    );


  }

  @Override
  protected void triggerEvents() {
  }

}
