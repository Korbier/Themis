package org.sc.themis.scene.ui;

public final class LabelBuilder extends ComponentBuilder<LabelBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;

  private String text = null;

  LabelBuilder(UIBuilder builder) {
    super(builder);
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
      pencil()
          .drawText(
              left + 2 * DEFAULT_BORDER_SIZE,
              top + 2 * DEFAULT_BORDER_SIZE,
              height - 4 * DEFAULT_BORDER_SIZE,
              this.text);
    }

  }

  @Override
  protected void triggerEvents() {
  }

}
