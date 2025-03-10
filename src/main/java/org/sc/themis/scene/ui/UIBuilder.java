package org.sc.themis.scene.ui;

import org.sc.themis.input.Input;
import org.sc.themis.scene.pencil.Pencil;

public class UIBuilder {

  private final Pencil pencil;
  private final UiState uiState = new UiState();

  public UIBuilder(Pencil pencil) {
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

  public ButtonBuilder button(String identifier, String text) {
    return new ButtonBuilder(this).identifier(identifier).text(text);
  }

  public ToggleButtonBuilder toggleButton(String identifier) {
    return new ToggleButtonBuilder(this).identifier(identifier);
  }

}
