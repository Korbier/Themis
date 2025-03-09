package org.sc.themis.scene.ui;

import java.util.function.Consumer;

public final class ButtonBuilder extends ComponentBuilder<ButtonBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;

  public static final String EVENT_ON_CLICK = "button.event.onclick";
  public static final String EVENT_ON_HOVER = "button.event.onHover";

  private String text = null;

  ButtonBuilder(UIBuilder builder) {
    super(builder);
  }

  ButtonBuilder text(String text) {
    this.text = text;
    return this;
  }

  public ButtonBuilder onClick(Consumer<UIBuilder> eventListener) {
    addEvent(EVENT_ON_CLICK, eventListener);
    return this;
  }

  public ButtonBuilder onHover(Consumer<UIBuilder> eventListener) {
    addEvent(EVENT_ON_HOVER, eventListener);
    return this;
  }

  @Override
  protected void draw() {

    pencil().drawRect(this.left(), this.top(), this.width(), this.height(), .2f, .2f, .2f);

    if (isHotItem()) {
      pencil()
          .drawRect(
              this.left() + DEFAULT_BORDER_SIZE,
              this.top() + DEFAULT_BORDER_SIZE,
              this.width() - 2 * DEFAULT_BORDER_SIZE,
              this.height() - 2 * DEFAULT_BORDER_SIZE,
              .2f,
              .2f,
              .2f);
    } else {
      pencil()
          .drawRect(
              this.left() + DEFAULT_BORDER_SIZE,
              this.top() + DEFAULT_BORDER_SIZE,
              this.width() - 2 * DEFAULT_BORDER_SIZE,
              this.height() - 2 * DEFAULT_BORDER_SIZE,
              .5f,
              .5f,
              .5f);
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

  private boolean shouldTriggerOnHoverEvent() {
    return isEventDefined(EVENT_ON_HOVER) && isHotItem();
  }

}
