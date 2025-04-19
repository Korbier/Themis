package org.sc.themis.scene.ui.component.combobox;

import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.scene.ui.UiBuilder;
import org.sc.themis.scene.ui.component.ComponentBuilder;
import org.sc.themis.scene.ui.component.LabelBuilder;

public class ComboboxSelectionBuilder extends ComponentBuilder<ComboboxSelectionBuilder> {

  public static final int DEFAULT_BORDER_SIZE = 1;
  public static final int DEFAULT_TEXT_MARGIN = 1;
  public static final int DEFAULT_BUTTON_MARGIN_SIZE = 2;
  public static final int DEFAULT_BUTTON_WIDTH = 16;

  private Color backgroundColor = Color.of("222222");
  private Color color = Color.of("555555");
  private Color hotColor = Color.of("777777");

  private final ComboboxBuilder combobox;
  private final LabelBuilder lblSelection;

  protected ComboboxSelectionBuilder(UiBuilder uiBuilder, ComboboxBuilder combobox) {
    super(uiBuilder);
    this.combobox = combobox;
    this.lblSelection = uiBuilder.label().identifier(uiBuilder.getIdentifier()).size(combobox.width(), combobox.height());
    child(this.lblSelection);
    addEvent(ComboboxBuilder.EVENT_ON_OPEN, (_,_) -> this.combobox.setOpen(!this.combobox.isOpen()) );
  }

  @Override
  protected void configure(int left, int top, int width, int height) {

    if (this.combobox.getSelection() != null) {
      this.lblSelection
          .position(left + DEFAULT_BORDER_SIZE + DEFAULT_TEXT_MARGIN, top + DEFAULT_BORDER_SIZE + DEFAULT_TEXT_MARGIN)
          .size(width - DEFAULT_BORDER_SIZE * 2 - DEFAULT_TEXT_MARGIN * 2, height - DEFAULT_BORDER_SIZE * 2 - DEFAULT_TEXT_MARGIN * 2)
          .text( this.combobox.getSelection().toString());
    }

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

    if (this.combobox.isOpen()) {
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

  }

  @Override
  protected void triggerEvents() {
    if (shouldTriggerOnOpenEvent()) {
      this.fireEvent(ComboboxBuilder.EVENT_ON_OPEN);
    }
  }

  private boolean shouldTriggerOnOpenEvent() {
    return isEventDefined(ComboboxBuilder.EVENT_ON_OPEN)
        && !state().isMouseDown()
        && isHotItem()
        && isActiveItem();
  }

}
