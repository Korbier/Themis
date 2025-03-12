package org.sc.viewer.gamestate.controller;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;

import java.util.UUID;
import org.sc.themis.input.Input;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.base.Controller;
import org.sc.themis.scene.pencil.Color;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.scene.ui.PanelBuilder;
import org.sc.themis.scene.ui.ToggleButtonBuilder;
import org.sc.themis.scene.ui.UIBuilder;
import org.sc.viewer.ViewerContext;
import org.sc.viewer.renderactivity.postprocess.postprocessor.ShowTBNPostprocessor;

public class UiController implements Controller {

  private final UIBuilder builder;

  private final PanelBuilder pnlConfiguration;

  public UiController(Pencil pencil, Scene scene, ViewerContext context) {

    this.builder = new UIBuilder(pencil);

    ToggleButtonBuilder tglTBN = this.builder.toggleButton(UUID.randomUUID().toString())
        .location(4, 28)
        .size(40, 16)
        .colorDefault(Color.of("CBD5E1"))
        .colorHot(Color.of("94A3B8"))
        .colorToggled(Color.of("7092BE"))
        .isToggledSupplier(() -> context.isPostProcessorEnabled(ShowTBNPostprocessor.IDENTIFIER))
        .onClick(builder -> context.getKeyMapping().execute(GLFW_KEY_F1));

    ToggleButtonBuilder tglDirectionalLight = this.builder.toggleButton(UUID.randomUUID().toString())
        .location(4, 48)
        .size(40, 16)
        .colorDefault(Color.of("CBD5E1"))
        .colorHot(Color.of("94A3B8"))
        .colorToggled(Color.of("7092BE"))
        .isToggledSupplier(() -> scene.getDirectionalLights().getFirst().isVisible())
        .onClick(builder -> context.getKeyMapping().execute(GLFW_KEY_1));

    this.pnlConfiguration = this.builder.panel(UUID.randomUUID().toString())
        .location(100, 100)
        .size(300, 400)
        .child(tglTBN, tglDirectionalLight);

  }

  @Override
  public void update(long tpf) {
    this.builder.begin();
    this.pnlConfiguration.build();
    this.builder.end();
  }

  @Override
  public void input(Input input, long tpf) {
    this.builder.input(input);
  }
}
