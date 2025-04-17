package org.sc.themis.scene.ui.component;

import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.scene.ui.UiBuilder;

import java.util.*;

public final class ComboboxBuilder extends ComponentBuilder<ComboboxBuilder> {

  public static final String EVENT_ON_OPEN = "combobox.event.onopen";

  public static final int DEFAULT_BORDER_SIZE = 1;
  public static final int DEFAULT_MARGIN_SIZE = 1;
  public static final int DEFAULT_BUTTON_MARGIN_SIZE = 2;
  public static final int DEFAULT_BUTTON_WIDTH = 16;

  private Color backgroundColor = Color.of("222222");
  private Color color = Color.of("555555");
  private Color hotColor = Color.of("777777");

  private LabelBuilder lblSelection;
  private Item selection = null;

  private ComboboxContentBuilder cbxContent;

  private boolean isOpen = false;

  public ComboboxBuilder(UiBuilder uiBuilder) {
    super(uiBuilder);
    this.lblSelection = uiBuilder.label();
    this.cbxContent = new ComboboxContentBuilder(uiBuilder, this).identifier(UUID.randomUUID().toString());
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

  public ComboboxBuilder selection(Item item) {
    if (this.cbxContent.content.contains(item)) {
      this.selection = item;
    }
    return this;
  }

  public ComboboxBuilder content(Item ... items) {
    this.cbxContent.content(items);
    return this;
  }


  public ComboboxBuilder selection(int index) {
    this.selection = this.cbxContent.content.get(index);
    return this;
  }

  public boolean isOpen() {
    return this.isOpen;
  }

  public ComboboxContentBuilder getComboboxContent() {
    return this.cbxContent;
  }

  @Override
  protected void configure(int left, int top, int width, int height) {
    addEvent(EVENT_ON_OPEN, _ -> this.isOpen = !this.isOpen );
  }

  @Override
  protected void draw(int left, int top, int width, int height) {

    pencil().reset();

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

  public static class ComboboxContentBuilder extends ComponentBuilder<ComboboxContentBuilder> {

    public final static int COMBOBOX_LAYER_INDEX = 15;

    public static final String EVENT_ON_SELECT = "combobox.event.onselect";

    private final UiBuilder uiBuilder;
    private ComboboxBuilder combo;

    private final List<Item> content = new ArrayList<>();
    private final Map<Item, LabelBuilder> lblComponents = new HashMap<>();

    protected ComboboxContentBuilder(UiBuilder uiBuilder, ComboboxBuilder combo) {
      super(uiBuilder, COMBOBOX_LAYER_INDEX);
      this.uiBuilder = uiBuilder;
      this.combo = combo;
    }

    public ComboboxContentBuilder content(Item ... items) {

      for (Item item : items) {
        LabelBuilder lblBuilder = this.uiBuilder.label(COMBOBOX_LAYER_INDEX).text(item.value.toString());
        child(lblBuilder);
        this.content.add(item);
        this.lblComponents.put(item, lblBuilder);
      }

      this.content.addAll(Arrays.asList(items));

      return this;

    }

    @Override
    protected void configure(int left, int top, int width, int height) {

      addEvent(EVENT_ON_SELECT, _ -> System.out.println("select") );

      size(this.combo.width(), this.content.size() * 20);
      position(this.combo.left(), this.combo.top() + this.combo.height());
      setHotRegion(
          this.combo.hotRegion[0],
          this.combo.hotRegion[1] + this.combo.height(),
          this.combo.width(),
          this.content.size() * 20
      );

      int i = 0;
      for (LabelBuilder lblBuilder : this.lblComponents.values()) {
        lblBuilder.position(this.combo.hotRegion[0] + DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE, this.combo.hotRegion[1] + DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE + i * 20);
        lblBuilder.size( this.combo.width() - 2 * DEFAULT_BORDER_SIZE - 2 * DEFAULT_MARGIN_SIZE, 20 - 2 * DEFAULT_BORDER_SIZE - 2 * DEFAULT_MARGIN_SIZE);
        lblBuilder.debug(true);
        i++;
      }

    }

    @Override
    protected void draw(int left, int top, int width, int height) {

      this.pencil().reset();

      this.pencil()
          .color(isHotItem() ? this.combo.hotColor : this.combo.color)
          .rect(left, top, width, height)
          .color(this.combo.backgroundColor)
          .rect(left + DEFAULT_BORDER_SIZE, top + DEFAULT_BORDER_SIZE, width - 2 * DEFAULT_BORDER_SIZE, height - 2 * DEFAULT_BORDER_SIZE);

    }

    @Override
    protected void triggerEvents() {
      if (shouldTriggerOnSelectEvent()) {
        this.fireEvent(EVENT_ON_SELECT);
      }
    }

    private boolean shouldTriggerOnSelectEvent() {
      return isEventDefined(EVENT_ON_SELECT)
          && !state().isMouseDown()
          && isHotItem()
          && isActiveItem();
    }

  }

}
