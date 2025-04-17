package org.sc.themis.scene.ui.component;

import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.scene.ui.UiBuilder;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ToggleButtonBuilder extends ComponentBuilder<ToggleButtonBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 1;
  public static final int DEFAULT_MARGIN_SIZE = 1;

  public static final String EVENT_ON_CLICK = "togglebutton.event.onclick";

  private Supplier<Boolean> isToggledSupplier = null;
  private boolean isToggled = false;

  private Color backgroundColor = Color.of("222222");
  private Color color = Color.of("555555");
  private Color hotColor = Color.of("777777");

  private Color buttonColor = Color.of("552222");
  private Color buttonHotColor = Color.of("772222");
  private Color buttonToggledColor = Color.of("225522");
  private Color buttonToggledHotColor = Color.of("227722");

  public ToggleButtonBuilder(UiBuilder builder) {
    super(builder);
  }

  public ToggleButtonBuilder onClick(Consumer<UiBuilder> eventListener) {
    addEvent(EVENT_ON_CLICK, eventListener);
    return this;
  }

  public ToggleButtonBuilder isToggledSupplier(Supplier<Boolean> isToggledSupplier) {
    this.isToggledSupplier = isToggledSupplier;
    return this;
  }

  public ToggleButtonBuilder color(Color color) {
    this.color = color;
    return this;
  }

  public ToggleButtonBuilder hotColor(Color color) {
    this.hotColor = color;
    return this;
  }

  public ToggleButtonBuilder backgroundColor(Color color) {
    this.backgroundColor = color;
    return this;
  }

  public void buttonColor(Color buttonColor) {
    this.buttonColor = buttonColor;
  }

  public void buttonHotColor(Color buttonHotColor) {
    this.buttonHotColor = buttonHotColor;
  }

  public void buttonToggledColor(Color buttonToggledColor) {
    this.buttonToggledColor = buttonToggledColor;
  }

  public void buttonToggledHotColor(Color buttonToggledHotColor) {
    this.buttonToggledHotColor = buttonToggledHotColor;
  }

  @Override
  protected void configure(int left, int top, int width, int height) {
    if (this.isToggledSupplier != null) {
      this.isToggled = Objects.requireNonNullElse(this.isToggledSupplier.get(), false);
    }
  }

  @Override
  protected void draw(int left, int top, int width, int height) {

      pencil().reset();

      //Cursor position
      float middle = (float) width / 2;
      float buttonX = this.isToggled ? left + DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE + middle : left + DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE;
      float buttonY = top + DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE;
      float buttonW = width - 2 * DEFAULT_BORDER_SIZE - 2 * DEFAULT_MARGIN_SIZE - middle;
      float buttonH = height - 2 * DEFAULT_BORDER_SIZE - 2 * DEFAULT_MARGIN_SIZE;

      pencil()
        .color(isHotItem() ? this.hotColor : this.color)
        .rect(left, top, width, height)
        .color(this.backgroundColor)
        .rect(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE, width - 2 * DEFAULT_BORDER_SIZE, height - 2 * DEFAULT_BORDER_SIZE)
        .color(isHotItem()
            ? isToggled ? this.buttonToggledHotColor : this.buttonHotColor
            : isToggled ? this.buttonToggledColor : this.buttonColor)
        .rect(buttonX, buttonY, buttonW, buttonH);

  }

  @Override
  protected void triggerEvents() {
    if (shouldTriggerOnClickEvent()) {
      this.fireEvent(EVENT_ON_CLICK);
    }
  }

  private boolean shouldTriggerOnClickEvent() {/*
    System.out.println("isEventDefined(EVENT_ON_CLICK)=" + isEventDefined(EVENT_ON_CLICK)
        + "; !state().isMouseDown()=" + !state().isMouseDown()
        + "; isHotItem()=" + isHotItem()
        + "; isActiveItem()=" + isActiveItem() );*/
    return isEventDefined(EVENT_ON_CLICK)
        && !state().isMouseDown()
        && isHotItem()
        && isActiveItem();
  }

}
