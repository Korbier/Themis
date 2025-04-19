package org.sc.themis.scene.ui;

import org.sc.themis.input.Input;
import org.sc.themis.renderer.pencil2d.Pencil2D;
import org.sc.themis.scene.ui.component.*;
import org.sc.themis.scene.ui.component.combobox.ComboboxBuilder;

import java.util.UUID;
import java.util.function.Supplier;

public class UiBuilder {

  public final BlankComponent blank;

  private final Pencil2D pencil2D;
  private final UiState uiState = new UiState();

  private Supplier<String> identifierSupplier = () -> UUID.randomUUID().toString();

  public UiBuilder(Pencil2D pencil2D) {
    this.pencil2D = pencil2D;
    this.blank = new BlankComponent(this);
  }

  public void begin() {
    uiState.setHotItem(null);
    this.pencil2D.clear();
  }

  public void end() {
    if (!uiState.isMouseDown()) {
      uiState.setActiveItem(null);
    } else {
      if (uiState.getActiveItem() == null) {
        uiState.setActiveItem(this.blank);
      }
    }
  }

  public void setIdentifierSupplier(Supplier<String> supplier) {
    this.identifierSupplier = supplier;
  }

  public void input(Input input) {
    this.uiState.setMouseX((int) input.getMousePosition().x);
    this.uiState.setMouseY((int) input.getMousePosition().y);
    this.uiState.setMouseDown(input.isLeftButtonPressed());
  }

  public Pencil2D pencil2D() {
    return this.pencil2D;
  }

  public UiState getState() {
    return this.uiState;
  }

  public String getIdentifier() {
    return identifierSupplier.get();
  }

  public ContainerBuilder container() {
    return new ContainerBuilder(this).identifier(getIdentifier());
  }

  public ButtonBuilder button(String text) {
    return new ButtonBuilder(this).identifier(getIdentifier()).text(text);
  }

  public ToggleButtonBuilder toggleButton() {
    return new ToggleButtonBuilder(this).identifier(getIdentifier());
  }

  public PanelBuilder panel() {
    return new PanelBuilder(this).identifier(getIdentifier());
  }

  public LabelBuilder label() {
    return new LabelBuilder(this).identifier(getIdentifier());
  }

  public LabelBuilder label(int layer) {
    return new LabelBuilder(this, layer).identifier(getIdentifier());
  }

  public <T> ComboboxBuilder<T> combobox() {
    return new ComboboxBuilder<T>(this).identifier(getIdentifier());
  }

}
