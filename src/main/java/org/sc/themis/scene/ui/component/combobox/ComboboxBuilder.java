package org.sc.themis.scene.ui.component.combobox;

import org.sc.themis.scene.ui.UiBuilder;
import org.sc.themis.scene.ui.component.ComponentBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ComboboxBuilder<C> extends ComponentBuilder<ComboboxBuilder<C>>  {

  public static final String EVENT_ON_OPEN = "combobox.event.onopen";

  private final ComboboxSelectionBuilder cmbSelection;
  private final ComboboxSelectorBuilder<C> cmbSelector;

  private final List<C> content = new ArrayList<>();
  private Supplier<C> selectionSupplier = null;
  private boolean isOpen = false;

  private Consumer<C> onSelectConsumer = null;

  public ComboboxBuilder(UiBuilder uiBuilder) {
    super(uiBuilder);
    this.cmbSelection = new ComboboxSelectionBuilder(uiBuilder, this).identifier(uiBuilder.getIdentifier());
    this.cmbSelector = new ComboboxSelectorBuilder<>(uiBuilder, this).identifier(uiBuilder.getIdentifier());
    child(this.cmbSelection);
  }

  public ComboboxBuilder<C> content(C ... items) {
    this.content.addAll(Arrays.asList(items));
    return this;
  }

  public List<C> content() {
    return this.content;
  }

  public ComboboxBuilder<C> onSelect(Consumer<C> consumer) {
    this.onSelectConsumer = consumer;
    return this;
  }

  public void triggerOnSelect(C item) {
    if (this.onSelectConsumer != null) {
      this.onSelectConsumer.accept(item);
    }
  }

  public void selectionSupplier(Supplier<C> supplier) {
    this.selectionSupplier = supplier;
  }

  public C getSelection() {
    return this.selectionSupplier != null ? this.selectionSupplier.get() : null;
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
