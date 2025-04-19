package org.sc.themis.scene.ui.component.combobox;

import org.sc.themis.scene.ui.UiBuilder;
import org.sc.themis.scene.ui.component.ComponentBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComboboxBuilder extends ComponentBuilder<ComboboxBuilder>  {

  public static final String EVENT_ON_OPEN = "combobox.event.onopen";

  private final ComboboxSelectionBuilder cmbSelection;
  private final ComboboxSelectorBuilder cmbSelector;

  private final List<ComboboxItem> content = new ArrayList<>();
  private ComboboxItem selection = null;
  private boolean isOpen = false;

  public ComboboxBuilder(UiBuilder uiBuilder) {
    super(uiBuilder);
    this.cmbSelection = new ComboboxSelectionBuilder(uiBuilder, this).identifier(uiBuilder.getIdentifier());
    this.cmbSelector = new ComboboxSelectorBuilder(uiBuilder, this).identifier(uiBuilder.getIdentifier());
    child(this.cmbSelection);
  }

  public ComboboxBuilder content(ComboboxItem ... items) {
    this.content.addAll(Arrays.asList(items));
    return this;
  }

  public List<ComboboxItem> content() {
    return this.content;
  }

  public ComboboxBuilder selection(ComboboxItem selection) {
    this.selection = selection;
    return this;
  }

  public ComboboxBuilder selection(int index) {
    this.selection = this.content.get(index);
    return this;
  }

  public ComboboxItem getSelection() {
    return this.selection;
  }

  public boolean isOpen() {
    return this.isOpen;
  }

  public void setOpen(boolean open) {
    this.isOpen = open;
  }

  @Override
  protected void configure(int left, int top, int width, int height) {

    remove(this.cmbSelector);

    if (this.isOpen()) {
      child(this.cmbSelector);
    }

    this.cmbSelection.position(left, top).size(width, height);
    this.cmbSelector.position(left, top + height).size(width, height * content.size());

  }

  @Override
  protected void draw(int left, int top, int width, int height) {

  }

  @Override
  protected void triggerEvents() {

  }

}
