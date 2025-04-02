package org.sc.themis.scene.ui.component;

import org.joml.Vector2f;
import org.sc.themis.renderer.resource.font.Font;
import org.sc.themis.scene.pencil.Color;
import org.sc.themis.scene.ui.UiBuilder;

public final class LabelBuilder extends ComponentBuilder<LabelBuilder> {

  private String text = null;
  private Color color = Color.of("ffffff");
  private int fontIdx = 0;

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

  public LabelBuilder font(int fontIdx) {
    this.fontIdx = fontIdx;
    return this;
  }

  @Override
  protected void configure(int left, int top, int width, int height) {
    //Nothing to do
  }

  @Override
  protected void draw(int left, int top, int width, int height) {

    int decal = 0;

    Font font = this.pencil().getFontRepository().get(this.fontIdx);
    if (height > font.getFontSize()) {
      decal = (height - font.getFontSize()) / 2;
    }

    if (this.text != null) {
      pencil().text(new Vector2f(left, top + decal), 0, this.color, this.text);
    }

  }

  @Override
  protected void triggerEvents() {
  }

}
