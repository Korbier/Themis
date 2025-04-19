package org.sc.themis.scene.ui.component.combobox;

import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.scene.ui.UiBuilder;
import org.sc.themis.scene.ui.component.ComponentBuilder;
import org.sc.themis.scene.ui.component.LabelBuilder;

import java.util.HashMap;
import java.util.Map;

public class ComboboxSelectorBuilder extends ComponentBuilder<ComboboxSelectorBuilder> {

  public final static int COMBOBOX_SELECTOR_LAYER_INDEX = 15;

  public static final String EVENT_ON_SELECT = "combobox.event.onselect";

  public static final int DEFAULT_BORDER_SIZE = 1;
  public static final int DEFAULT_MARGIN_SIZE = 1;

  private Color backgroundColor = Color.of("222222");
  private Color color = Color.of("555555");
  private Color hotColor = Color.of("777777");

  private final UiBuilder builder;
  private final ComboboxBuilder combobox;
  private Map<LabelBuilder, ComboboxItem> labels = null;

  protected ComboboxSelectorBuilder(UiBuilder uiBuilder, ComboboxBuilder combobox) {

    super(uiBuilder, COMBOBOX_SELECTOR_LAYER_INDEX);

    this.builder = uiBuilder;
    this.combobox = combobox;

  }

  @Override
  protected void configure(int left, int top, int width, int height) {

    if (this.labels == null) {

      this.labels = new HashMap<>();

      for (int i=0; i<this.combobox.content().size(); i++) {

        ComboboxItem item = this.combobox.content().get(i);

        LabelBuilder lbl = this.builder.label(COMBOBOX_SELECTOR_LAYER_INDEX)
            .activable(true)
            .position(DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE, i * 20 + DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE)
            .size(width - 2 * (DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE), 20 - 2 * (DEFAULT_BORDER_SIZE + DEFAULT_MARGIN_SIZE))
            .text(this.combobox.content().get(i).label())
            .onClick((_, c) -> {
              this.combobox.selection(this.labels.get(c));
              this.combobox.setOpen(false);
            } );

        child(lbl);

        this.labels.put(lbl, item);

      }

    }

  }

  @Override
  protected void draw(int left, int top, int width, int height) {

    this.pencil().reset();

    this.pencil()
        .color(isHotItem() ? hotColor : color)
        .rect(left, top, width, height)
        .color(backgroundColor)
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
