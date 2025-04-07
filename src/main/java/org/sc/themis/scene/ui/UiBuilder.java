package org.sc.themis.scene.ui;

import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.pencil2d.Pencil2D;
import org.sc.themis.renderer.pencil2d.Pencil2DLayer;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.scene.ui.component.*;

import java.util.UUID;
import java.util.function.Supplier;

public class UiBuilder {

  private final Pencil2D pencil;
  private final UiState uiState = new UiState();

  private Supplier<String> identifierSupplier = () -> UUID.randomUUID().toString();
  private Pencil2DLayer background;
  private Pencil2DLayer foreground;

  public UiBuilder(Pencil2D pencil) {
    this.pencil = pencil;
    this.background = this.pencil.layer(0);
    this.foreground = this.pencil.layer(1);
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

  public Pencil2DLayer background() {
    return this.background;
  }

  public Pencil2DLayer foreground() {
    return this.foreground;
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

  public ComboboxBuilder combobox() {
    return new ComboboxBuilder(this).identifier(identifierSupplier.get());
  }

}
