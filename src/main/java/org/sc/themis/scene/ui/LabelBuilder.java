package org.sc.themis.scene.ui;

import org.joml.Vector2f;
import org.sc.themis.scene.pencil.Color;
import org.sc.themis.shared.resource.FontInstance;

public final class LabelBuilder extends ComponentBuilder<LabelBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;

  private String text = null;
  private FontInstance font = FontInstance.VERDANA_12;

  LabelBuilder(UIBuilder builder) {
    super(builder);
  }

  public LabelBuilder text(String text) {
    this.text = text;
    return this;
  }

  public LabelBuilder font(FontInstance font) {
    this.font = font;
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
          this.font, Color.of("000000"), this.text
      );
    }

  }

  @Override
  protected void triggerEvents() {
  }

}
