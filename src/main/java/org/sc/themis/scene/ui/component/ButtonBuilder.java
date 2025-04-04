package org.sc.themis.scene.ui.component;

import java.util.function.Consumer;
import org.joml.Vector2f;
import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.scene.ui.UiBuilder;


public final class ButtonBuilder extends ComponentBuilder<ButtonBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;

  public static final String EVENT_ON_CLICK = "button.event.onclick";

  private String text = null;
  private Color clrDefault = Color.of("ffffff");
  private Color clrHot = null;
  private Color clrActive = null;

  public ButtonBuilder(UiBuilder builder) {
    super(builder);
  }

  public ButtonBuilder text(String text) {
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

  public ButtonBuilder onClick(Consumer<UiBuilder> eventListener) {
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
/*
    pencil().rect(new Vector2f(left, top), new Vector2f(width, height), activeColor);

    if (isHotItem()) {
      if (isActiveItem()) {
        pencil().rect(
            new Vector2f(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE),
            new Vector2f(width - 2 * DEFAULT_BORDER_SIZE, height - 2 * DEFAULT_BORDER_SIZE),
            activeColor
        );
      } else {
        pencil().rect(
            new Vector2f(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE),
            new Vector2f(width - 2 * DEFAULT_BORDER_SIZE, height - 2 * DEFAULT_BORDER_SIZE),
            hotColor
        );
      }
    } else {
      pencil().rect(
          new Vector2f(left + DEFAULT_BORDER_SIZE,top + DEFAULT_BORDER_SIZE),
          new Vector2f(width - 2 * DEFAULT_BORDER_SIZE, height - 2 * DEFAULT_BORDER_SIZE),
          defaultColor
      );
    }

    if (this.text != null) {
      pencil().text(
          new Vector2f(left + 2 * DEFAULT_BORDER_SIZE, top + 2 * DEFAULT_BORDER_SIZE),
          0, Color.of("FF0000"),
          this.text
      );
    }
*/
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
