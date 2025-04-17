package org.sc.themis.scene.ui;

import org.sc.themis.input.Input;
import org.sc.themis.renderer.pencil2d.Pencil2D;
import org.sc.themis.scene.ui.component.*;

import java.util.UUID;
import java.util.function.Supplier;

public class UiBuilder {

  private final Pencil2D pencil2D;
  private final UiState uiState = new UiState();

  private Supplier<String> identifierSupplier = () -> UUID.randomUUID().toString();

  public UiBuilder(Pencil2D pencil2D) {
    this.pencil2D = pencil2D;
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
        uiState.setActiveItem("NOT_AVAILABLE");
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

  public ContainerBuilder container() {
    return new ContainerBuilder(this).identifier(identifierSupplier.get());
  }

  public ButtonBuilder button(String text) {
    return new ButtonBuilder(this).identifier(identifierSupplier.get()).text(text);
  }

  public ToggleButtonBuilder toggleButton() {
    return new ToggleButtonBuilder(this).identifier(identifierSupplier.get());
  }

  public PanelBuilder panel() {
    return new PanelBuilder(this).identifier(identifierSupplier.get());
  }

  public LabelBuilder label() {
    return new LabelBuilder(this).identifier(identifierSupplier.get());
  }

  public LabelBuilder label(int layer) {
    return new LabelBuilder(this, layer).identifier(identifierSupplier.get());
  }

  public ComboboxBuilder combobox() {
    return new ComboboxBuilder(this).identifier(identifierSupplier.get());
  }

}
