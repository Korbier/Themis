package org.sc.themis.scene.ui.component;

import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.renderer.resource.font.Font;
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

    if (!areFontsAvailable()) {
      return;
    }

    int decal = 0;

    Font font = getFonts().get(this.fontIdx);

    if (height > font.getFontSize()) {
      decal = (height - font.getFontSize()) / 2;
    }

    if (this.text != null) {
      pencil()
          .color(this.color)
          .font(this.fontIdx)
          .text(left, top + decal, this.text);
    }

  }

  @Override
  protected void triggerEvents() {
  }

}
