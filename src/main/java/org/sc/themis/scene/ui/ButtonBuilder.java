package org.sc.themis.scene.ui;

import org.sc.themis.scene.pencil.Color;

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
  protected void configure(int left, int top, int width, int height) {
    //Nothing to do
  }

  @Override
  protected void draw(int left, int top, int width, int height) {

    Color defaultColor = this.clrDefault;
    Color hotColor = this.clrHot != null ? this.clrHot : this.clrDefault;
    Color activeColor = this.clrActive != null ? this.clrActive : this.clrDefault;

    pencil().drawRect(
        left, top, width, height,
        activeColor.r(), activeColor.b(), activeColor.g()
    );

    if (isHotItem()) {
      if (isActiveItem()) {
        pencil()
            .drawRect(
                left + DEFAULT_BORDER_SIZE,
                top + DEFAULT_BORDER_SIZE,
                width - 2 * DEFAULT_BORDER_SIZE,
                height - 2 * DEFAULT_BORDER_SIZE,
                activeColor.r(), activeColor.b(), activeColor.g());
      } else {
        pencil()
            .drawRect(
                left + DEFAULT_BORDER_SIZE,
                top + DEFAULT_BORDER_SIZE,
                width - 2 * DEFAULT_BORDER_SIZE,
                height - 2 * DEFAULT_BORDER_SIZE,
                hotColor.r(), hotColor.b(), hotColor.g());
      }
    } else {
      pencil()
          .drawRect(
              left + DEFAULT_BORDER_SIZE,
              top + DEFAULT_BORDER_SIZE,
              width - 2 * DEFAULT_BORDER_SIZE,
              height - 2 * DEFAULT_BORDER_SIZE,
              defaultColor.r(), defaultColor.b(), defaultColor.g());
    }

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
