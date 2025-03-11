package org.sc.themis.scene.ui;

import java.util.function.Consumer;

public final class ButtonBuilder extends ComponentBuilder<ButtonBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;

  public static final String EVENT_ON_CLICK = "button.event.onclick";

  private String text = null;
  private Color clrDefault = Color.of("ffffff");
  private Color clrHot = null;
  private Color clrActive = null;

  ButtonBuilder(UIBuilder builder) {
    super(builder);
  }

  ButtonBuilder text(String text) {
    this.text = text;
    return this;
  }

  public ButtonBuilder colorDefault(Color color) {
    this.clrDefault = color;
    return this;
  }

  public ButtonBuilder colorHot(Color color) {
    this.clrHot = color;
    return this;
  }

  public ButtonBuilder colorActive(Color color) {
    this.clrActive = color;
    return this;
  }

  public ButtonBuilder onClick(Consumer<UIBuilder> eventListener) {
    addEvent(EVENT_ON_CLICK, eventListener);
    return this;
  }

  @Override
  protected void checkInput() {
    //Nothing to do
  }

  @Override
  protected void draw() {

    Color defaultColor = this.clrDefault;
    Color hotColor = this.clrHot != null ? this.clrHot : this.clrDefault;
    Color activeColor = this.clrActive != null ? this.clrActive : this.clrDefault;

    pencil().drawRect(
        this.left(), this.top(), this.width(), this.height(),
        activeColor.r(), activeColor.b(), activeColor.g()
    );

    if (isHotItem()) {
      if (isActiveItem()) {
        pencil()
            .drawRect(
                this.left() + DEFAULT_BORDER_SIZE,
                this.top() + DEFAULT_BORDER_SIZE,
                this.width() - 2 * DEFAULT_BORDER_SIZE,
                this.height() - 2 * DEFAULT_BORDER_SIZE,
                activeColor.r(), activeColor.b(), activeColor.g());
      } else {
        pencil()
            .drawRect(
                this.left() + DEFAULT_BORDER_SIZE,
                this.top() + DEFAULT_BORDER_SIZE,
                this.width() - 2 * DEFAULT_BORDER_SIZE,
                this.height() - 2 * DEFAULT_BORDER_SIZE,
                hotColor.r(), hotColor.b(), hotColor.g());
      }
    } else {
      pencil()
          .drawRect(
              this.left() + DEFAULT_BORDER_SIZE,
              this.top() + DEFAULT_BORDER_SIZE,
              this.width() - 2 * DEFAULT_BORDER_SIZE,
              this.height() - 2 * DEFAULT_BORDER_SIZE,
              defaultColor.r(), defaultColor.b(), defaultColor.g());
    }

    if (this.text != null) {
      pencil()
          .drawText(
              this.left() + 2 * DEFAULT_BORDER_SIZE,
              this.top() + 2 * DEFAULT_BORDER_SIZE,
              this.height() - 4 * DEFAULT_BORDER_SIZE,
              this.text);
    }

  }

  @Override
  protected void triggerEvents() {
    if (shouldTriggerOnClickEvent()) {
      this.fireEvent(EVENT_ON_CLICK);
    }
  }

  private boolean shouldTriggerOnClickEvent() {
    return isEventDefined(EVENT_ON_CLICK)
        && !state().isMouseDown()
        && isHotItem()
        && isActiveItem();
  }

}
