package org.sc.themis.scene.ui;

import org.joml.Vector2f;
import org.sc.themis.scene.pencil.Color;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ToggleButtonBuilder extends ComponentBuilder<ToggleButtonBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 2;

  public static final String EVENT_ON_CLICK = "togglebutton.event.onclick";

  private Supplier<Boolean> isToggledSupplier = null;
  private boolean isToggled = false;
  private Color clrDefault = Color.of("ffffff");
  private Color clrHot = null;
  private Color clrToggled = null;
  private Color clrButton = null;

  ToggleButtonBuilder(UIBuilder builder) {
    super(builder);
  }

  public ToggleButtonBuilder onClick(Consumer<UIBuilder> eventListener) {
    addEvent(EVENT_ON_CLICK, eventListener);
    return this;
  }

  public ToggleButtonBuilder isToggledSupplier(Supplier<Boolean> isToggledSupplier) {
    this.isToggledSupplier = isToggledSupplier;
    return this;
  }

  public ToggleButtonBuilder colorDefault(Color color) {
    this.clrDefault = color;
    return this;
  }

  public ToggleButtonBuilder colorHot(Color color) {
    this.clrHot = color;
    return this;
  }

  public ToggleButtonBuilder colorToggled(Color color) {
    this.clrToggled = color;
    return this;
  }

  public ToggleButtonBuilder colorButton(Color color) {
    this.clrButton = color;
    return this;
  }

  @Override
  protected void configure(int left, int top, int width, int height) {
    if (this.isToggledSupplier != null) {
      this.isToggled = this.isToggledSupplier.get();
    }
  }

  @Override
  protected void draw(int left, int top, int width, int height) {

    Color defaultColor = this.clrDefault;
    Color hotColor = this.clrHot != null ? this.clrHot : this.clrDefault;
    Color toggledColor = this.clrToggled != null ? this.clrToggled : this.clrDefault;
    Color buttonColor = this.clrButton != null ? this.clrButton : Color.of("ffffff");

    float middle = (float) width / 2;
    float btnTop = top + DEFAULT_BORDER_SIZE;
    float btnWidth = width - 2 * DEFAULT_BORDER_SIZE - middle;
    float btnHeight = height - 2 * DEFAULT_BORDER_SIZE;

    if (this.isToggled) {
      pencil().rect(
          new Vector2f(left, top),
          new Vector2f(width, height),
          toggledColor
      );

      pencil().rect(
          new Vector2f(left + DEFAULT_BORDER_SIZE + middle, btnTop),
          new Vector2f(btnWidth, btnHeight),
          buttonColor
      );

    } else {

      if (isHotItem()) {
        pencil().rect(
            new Vector2f(left, top),
            new Vector2f(width, height),
            hotColor
        );
      } else {
        pencil().rect(
            new Vector2f(left, top),
            new Vector2f(width, height),
            defaultColor
        );
      }

      pencil().rect(
          new Vector2f(left + DEFAULT_BORDER_SIZE, btnTop),
          new Vector2f(btnWidth, btnHeight),
          buttonColor
      );

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
