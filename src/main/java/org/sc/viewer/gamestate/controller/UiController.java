package org.sc.viewer.gamestate.controller;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;

import java.util.UUID;
import org.sc.themis.input.Input;
import org.sc.themis.scene.base.Controller;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.scene.ui.ButtonBuilder;
import org.sc.themis.scene.ui.ComponentBuilder;
import org.sc.themis.scene.ui.UIBuilder;
import org.sc.viewer.ViewerContext;

public class UiController implements Controller {

  private final ViewerContext context;
  private final UIBuilder builder;

  private final ButtonBuilder btnTBN;

  public UiController(Pencil pencil, ViewerContext context) {

    this.context = context;
    this.builder = new UIBuilder(pencil);

    this.btnTBN = this.builder.button(UUID.randomUUID().toString(), "TBN")
                              .location(2, 2)
                              .size(120, 22)
                              .onClick(builder -> context.getKeyMapping().execute(GLFW_KEY_F1));

  }

  @Override
  public void update(long tpf) {

    this.builder.begin();
    this.btnTBN.build();

    this.builder.end();
  }

  @Override
  public void input(Input input, long tpf) {
    this.builder.input(input);
  }
}
