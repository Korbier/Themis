package org.sc.themis.scene.ui;

import java.util.function.Consumer;

public final class SwitchButtonBuilder extends ComponentBuilder<SwitchButtonBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;

  public static final String EVENT_ON_CLICK = "switchbutton.event.onclick";

  SwitchButtonBuilder(UIBuilder builder) {
    super(builder);
  }

  public SwitchButtonBuilder onClick(Consumer<UIBuilder> eventListener) {
    addEvent(EVENT_ON_CLICK, eventListener);
    return this;
  }

  @Override
  protected void draw() {

    pencil().drawRect(this.left(), this.top(), this.width(), this.height(), .2f, .2f, .2f);

    if (isHotItem()) {
      if (isActiveItem()) {
        pencil()
            .drawRect(
                this.left() + DEFAULT_BORDER_SIZE,
                this.top() + DEFAULT_BORDER_SIZE,
                this.width() - 2 * DEFAULT_BORDER_SIZE,
                this.height() - 2 * DEFAULT_BORDER_SIZE,
                .5f,
                .5f,
                .5f);
      } else {
        pencil()
            .drawRect(
                this.left() + DEFAULT_BORDER_SIZE,
                this.top() + DEFAULT_BORDER_SIZE,
                this.width() - 2 * DEFAULT_BORDER_SIZE,
                this.height() - 2 * DEFAULT_BORDER_SIZE,
                .2f,
                .2f,
                .2f);
      }
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

  }

  @Override
  protected void triggerEvents() {
    if (shouldTriggerOnClickEvent()) {
      this.fireEvent(EVENT_ON_CLICK);
    }
  }

  private boolean shouldTriggerOnClickEvent() {
    return isEventDefined(EVENT_ON_CLICK)
        && state().isMouseDown()
        && isHotItem()
        && isActiveItem();
  }
}
