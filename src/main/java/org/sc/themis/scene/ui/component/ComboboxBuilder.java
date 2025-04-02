package org.sc.themis.scene.ui.component;

import org.joml.Vector2f;
import org.sc.themis.scene.pencil.Color;
import org.sc.themis.scene.ui.UiBuilder;

public final class ComboboxBuilder extends ComponentBuilder<ComboboxBuilder> {

  public static final String EVENT_ON_OPEN = "combobox.event.onopen";

  private int [] region = new int[4];
  private boolean isOpen = false;

  public ComboboxBuilder(UiBuilder uiBuilder) {
    super(uiBuilder);
  }

  @Override
  protected void configure(int left, int top, int width, int height) {
    addEvent(EVENT_ON_OPEN, _ -> this.isOpen = !this.isOpen );
  }

  @Override
  protected void draw(int left, int top, int width, int height) {

    Color defaultColor = Color.of("CBD5E1");
    Color hotColor = Color.of("94A3B8");

    if (isHotItem()) {
      pencil().rect(new Vector2f(left, top), new Vector2f(width, height), hotColor);
    } else {
      pencil().rect(new Vector2f(left, top), new Vector2f(width, height), defaultColor);
    }

    if (this.isOpen) {
      pushRegion();
      //Arrow
      pencil().triangle(
          new Vector2f(left + (width - 16), top + 2),
          new Vector2f(left + (width - 2), top + 2),
          new Vector2f(left + (width - 9), top + (height - 2)),
          Color.of("ffffff")
      );
      //Combobox content
      pencil().rect(new Vector2f(left, top + height), new Vector2f(width, 120), defaultColor);

      pushRegion();
      setRegion(left, top + height, width, 120);

    } else {

      popRegion();
      //Arrow
      pencil().triangle(
          new Vector2f(left + (width - 16), top + ((float) height / 2)),
          new Vector2f(left + (width - 2), top + 2),
          new Vector2f(left + (width - 2), top + (height - 2)),
          Color.of("ffffff")
      );
    }

    pencil().text(new Vector2f(left + 2, top + 2), 0, Color.of("000000"), "test");

  }

  @Override
  protected void triggerEvents() {
    if (shouldTriggerOnOpenEvent()) {
      this.fireEvent(EVENT_ON_OPEN);
    }
  }

  private boolean shouldTriggerOnOpenEvent() {
    return isEventDefined(EVENT_ON_OPEN)
        && !state().isMouseDown()
        && isHotItem()
        && isActiveItem();
  }
}
