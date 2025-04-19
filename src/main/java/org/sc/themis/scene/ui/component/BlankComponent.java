package org.sc.themis.scene.ui.component;

import org.sc.themis.scene.ui.UiBuilder;

public class BlankComponent extends ComponentBuilder<BlankComponent> {

  public BlankComponent(UiBuilder uiBuilder) {
    super(uiBuilder);
  }

  @Override
  protected void configure(int left, int top, int width, int height) {

  }

  @Override
  protected void draw(int left, int top, int width, int height) {

  }

  @Override
  protected void triggerEvents() {

  }
}
