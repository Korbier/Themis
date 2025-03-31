package org.sc.themis.scene.ui;

import org.sc.themis.input.Input;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.scene.ui.component.ButtonBuilder;
import org.sc.themis.scene.ui.component.LabelBuilder;
import org.sc.themis.scene.ui.component.PanelBuilder;
import org.sc.themis.scene.ui.component.ToggleButtonBuilder;

import java.util.UUID;
import java.util.function.Supplier;

public class UiBuilder {

  private final Pencil pencil;
  private final UiState uiState = new UiState();

  private Supplier<String> identifierSupplier = () -> UUID.randomUUID().toString();

  public UiBuilder(Pencil pencil) {
    this.pencil = pencil;
  }

  public void begin() {
    uiState.setHotItem(null);
    this.pencil.clear();
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

  public Pencil getPencil() {
    return this.pencil;
  }

  public UiState getState() {
    return this.uiState;
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

}
