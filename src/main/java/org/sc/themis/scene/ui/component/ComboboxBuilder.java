package org.sc.themis.scene.ui.component;

import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.renderer.pencil2d.Pencil2D;
import org.sc.themis.renderer.pencil2d.Pencil2DLayer;
import org.sc.themis.scene.ui.UiBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ComboboxBuilder extends ComponentBuilder<ComboboxBuilder> {

  public static final String EVENT_ON_OPEN = "combobox.event.onopen";

  public static final int DEFAULT_BORDER_SIZE = 1;
  public static final int DEFAULT_MARGIN_SIZE = 1;
  public static final int DEFAULT_BUTTON_MARGIN_SIZE = 2;
  public static final int DEFAULT_BUTTON_WIDTH = 16;

  public final static int COMBOBOX_LAYER_INDEX = 15;

  private Color backgroundColor = Color.of("222222");
  private Color color = Color.of("555555");
  private Color hotColor = Color.of("777777");

  private LabelBuilder lblSelection;
  private Pencil2DLayer contentPencil;

  private boolean isOpen = false;
  private Item selection = null;
  private final List<Item> content = new ArrayList<>();

  public ComboboxBuilder(UiBuilder uiBuilder) {
    super(uiBuilder);
    this.lblSelection = uiBuilder.label();
    this.contentPencil = uiBuilder.pencil2D().layer(COMBOBOX_LAYER_INDEX);
  }

  public ComboboxBuilder color(Color color) {
    this.color = color;
    return this;
  }

  public ComboboxBuilder hotColor(Color color) {
    this.hotColor = color;
    return this;
  }

  public ComboboxBuilder backgroundColor(Color color) {
    this.backgroundColor = color;
    return this;
  }

  public ComboboxBuilder font(int index) {
    this.lblSelection.font(index);
    return this;
  }

  public ComboboxBuilder content(Item ... items) {
    this.content.addAll(Arrays.asList(items));
    return this;
  }

  public ComboboxBuilder selection(Item item) {
    if (this.content.contains(item)) {
      this.selection = item;
    }
    return this;
  }

  public ComboboxBuilder selection(int index) {
    this.selection = this.content.get(index);
    return this;
  }

  public boolean isOpen() {
    return this.isOpen;
  }

  @Override
  protected void configure(int left, int top, int width, int height) {
    addEvent(EVENT_ON_OPEN, _ -> this.isOpen = !this.isOpen );
  }

  @Override
  protected void draw(int left, int top, int width, int height) {

    pencil().reset();
    this.contentPencil.reset();

    float buttonAX = left + width - DEFAULT_BORDER_SIZE - DEFAULT_BUTTON_MARGIN_SIZE - DEFAULT_BUTTON_WIDTH;
    float buttonAY = top + (float) height / 2;
    float buttonBX = left + width - DEFAULT_BORDER_SIZE - DEFAULT_BUTTON_MARGIN_SIZE;
    float buttonBY = top + DEFAULT_BORDER_SIZE + DEFAULT_BUTTON_MARGIN_SIZE;
    float buttonCX = left + width - DEFAULT_BORDER_SIZE - DEFAULT_BUTTON_MARGIN_SIZE;
    float buttonCY = top + height - DEFAULT_BORDER_SIZE - DEFAULT_BUTTON_MARGIN_SIZE;

    if (isOpen) {
      buttonAX = left + width - DEFAULT_BORDER_SIZE - DEFAULT_BUTTON_MARGIN_SIZE - DEFAULT_BUTTON_WIDTH;
      buttonAY = top + DEFAULT_BORDER_SIZE + DEFAULT_BUTTON_MARGIN_SIZE;
      buttonBX = left + width - DEFAULT_BORDER_SIZE - DEFAULT_BUTTON_MARGIN_SIZE;
      buttonBY = top + DEFAULT_BORDER_SIZE + DEFAULT_BUTTON_MARGIN_SIZE;
      buttonCX = left + width - DEFAULT_BORDER_SIZE - DEFAULT_BUTTON_MARGIN_SIZE - (float) DEFAULT_BUTTON_WIDTH / 2;
      buttonCY = top + height - DEFAULT_BORDER_SIZE - DEFAULT_BUTTON_MARGIN_SIZE;
    }

    pencil()
        .color(isHotItem() ? this.hotColor : this.color)
        .rect(left, top, width, height)
        .color(this.backgroundColor)
        .rect(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE, width - 2 * DEFAULT_BORDER_SIZE, height - 2 * DEFAULT_BORDER_SIZE)
        .color(isHotItem() ? this.hotColor : this.color)
        .triangle(buttonAX, buttonAY, buttonBX, buttonBY, buttonCX, buttonCY)
    ;

    if (this.selection != null) {
      this.lblSelection
          .text(this.selection.value.toString())
          .draw(
              left + DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE,
              top + DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE,
              width - 2 * DEFAULT_BORDER_SIZE - 2 * DEFAULT_BUTTON_MARGIN_SIZE - DEFAULT_BUTTON_WIDTH,
              height - 2 * DEFAULT_BORDER_SIZE - 2 * DEFAULT_MARGIN_SIZE);
    }

    if (isOpen) {
      float contentHeight = this.content.size() * 20;
      this.contentPencil
          .color(isHotItem() ? this.hotColor : this.color)
          .rect(left, top + height, width, contentHeight)
          .color(this.backgroundColor)
          .rect(left + DEFAULT_BORDER_SIZE, top + height + DEFAULT_BORDER_SIZE, width - 2 * DEFAULT_BORDER_SIZE, contentHeight - 2 * DEFAULT_BORDER_SIZE);
    }


/*
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
*/
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

  public static record Item(String name, Object value) {}

}
