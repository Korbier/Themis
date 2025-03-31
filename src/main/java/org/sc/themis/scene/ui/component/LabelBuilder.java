package org.sc.themis.scene.ui.component;

import org.joml.Vector2f;
import org.sc.themis.scene.pencil.Color;
import org.sc.themis.scene.ui.UiBuilder;

public final class LabelBuilder extends ComponentBuilder<LabelBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;

  private String text = null;
  private Color color = Color.of("ffffff");

  public LabelBuilder(UiBuilder builder) {
    super(builder);
  }

  public LabelBuilder color(Color color) {
    this.color = color;
    return this;
  }

  public LabelBuilder text(String text) {
    this.text = text;
    return this;
  }

  @Override
  protected void configure(int left, int top, int width, int height) {
    //Nothing to do
  }

  @Override
  protected void draw(int left, int top, int width, int height) {

    if (this.text != null) {
      pencil().text(
          new Vector2f(left + 2 * DEFAULT_BORDER_SIZE, top + 2 * DEFAULT_BORDER_SIZE),
          0, this.color, this.text
      );
    }

  }

  @Override
  protected void triggerEvents() {
  }

}
