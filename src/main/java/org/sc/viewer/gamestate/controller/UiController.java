package org.sc.viewer.gamestate.controller;

import java.util.Optional;
import java.util.UUID;
import org.sc.themis.input.Input;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.base.Controller;
import org.sc.themis.scene.base.geometry.Model;
import org.sc.themis.scene.pencil.Color;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.scene.ui.LabelBuilder;
import org.sc.themis.scene.ui.PanelBuilder;
import org.sc.themis.scene.ui.ToggleButtonBuilder;
import org.sc.themis.scene.ui.UIBuilder;
import org.sc.viewer.ViewerContext;
import org.sc.viewer.renderactivity.geometry.material.TextureMaterial;
import org.sc.viewer.renderactivity.postprocess.postprocessor.ShowTBNPostprocessor;

import static org.lwjgl.glfw.GLFW.*;

public class UiController implements Controller {

  private final UIBuilder builder;
  private final LabelBuilder lblElapsed;
  private final PanelBuilder pnlConfiguration;

  public UiController(Pencil pencil, Scene scene, ViewerContext context) {

    this.builder = new UIBuilder(pencil);

    this.lblElapsed = this.builder.label(UUID.randomUUID().toString())
        .location(2, 2)
        .size(40, 200)
        .color(Color.of("FFFFFF"))
        .text("fps : -");

    LabelBuilder lblTBN = this.builder.label(UUID.randomUUID().toString())
        .location(2, 26).size(40, 20).text("TBN");

    ToggleButtonBuilder tglTBN = this.builder.toggleButton(UUID.randomUUID().toString())
        .location(200 - 44, 28)
        .size(40, 16)
        .colorDefault(Color.of("CBD5E1"))
        .colorHot(Color.of("94A3B8"))
        .colorToggled(Color.of("7092BE"))
        .isToggledSupplier(() -> context.isPostProcessorEnabled(ShowTBNPostprocessor.IDENTIFIER))
        .onClick(_ -> context.getKeyMapping().execute(GLFW_KEY_F1));

    LabelBuilder lblDirectionalLight = this.builder.label(UUID.randomUUID().toString())
        .location(2, 48)
        .size(40, 20)
        .text("Directional light");

    ToggleButtonBuilder tglDirectionalLight =
        this.builder.toggleButton(UUID.randomUUID().toString())
        .location(200 - 44, 48)
        .size(40, 16)
        .colorDefault(Color.of("CBD5E1"))
        .colorHot(Color.of("94A3B8"))
        .colorToggled(Color.of("7092BE"))
        .isToggledSupplier(() -> scene.getDirectionalLights().getFirst().isVisible())
        .onClick(_ -> context.getKeyMapping().execute(GLFW_KEY_1));

    LabelBuilder lblPointLight = this.builder.label(UUID.randomUUID().toString())
        .location(2, 68)
        .size(40, 20)
        .text("Point light");

    ToggleButtonBuilder tglPointLight =
        this.builder.toggleButton(UUID.randomUUID().toString())
            .location(200 - 44, 68)
            .size(40, 16)
            .colorDefault(Color.of("CBD5E1"))
            .colorHot(Color.of("94A3B8"))
            .colorToggled(Color.of("7092BE"))
            .isToggledSupplier(() -> scene.getPointLights().getFirst().isVisible())
            .onClick(_ -> context.getKeyMapping().execute(GLFW_KEY_2));

    LabelBuilder lblSpotLight = this.builder.label(UUID.randomUUID().toString())
        .location(2, 88)
        .size(40, 20)
        .text("Spot light");

    ToggleButtonBuilder tglSpotLight =
        this.builder.toggleButton(UUID.randomUUID().toString())
            .location(200 - 44, 88)
            .size(40, 16)
            .colorDefault(Color.of("CBD5E1"))
            .colorHot(Color.of("94A3B8"))
            .colorToggled(Color.of("7092BE"))
            .isToggledSupplier(() -> scene.getSpotLights().getFirst().isVisible())
            .onClick(_ -> context.getKeyMapping().execute(GLFW_KEY_3));

    LabelBuilder lblMaterial = this.builder.label(UUID.randomUUID().toString())
        .location(2, 108)
        .size(40, 20)
        .text("Color / Texture");

    ToggleButtonBuilder tglMaterial =
        this.builder.toggleButton(UUID.randomUUID().toString())
            .location(200 - 44, 108)
            .size(40, 16)
            .colorDefault(Color.of("CBD5E1"))
            .colorHot(Color.of("94A3B8"))
            .colorToggled(Color.of("7092BE"))
            .isToggledSupplier(() -> {
              Optional<Model> oModel = scene.getModels().stream().findFirst();
              if (oModel.isEmpty()) return false;
              return oModel.get().getMaterial().isPresent() && oModel.get().getMaterial().get().equals(TextureMaterial.MATERIAL_ID);
            } )
            .onClick(_ -> context.getKeyMapping().execute(GLFW_KEY_4));

    this.pnlConfiguration = this.builder.panel(UUID.randomUUID().toString())
        .location(2, 30)
        .size(200, 300)
        .child(
            lblTBN, tglTBN,
            lblDirectionalLight, tglDirectionalLight,
            lblPointLight, tglPointLight,
            lblSpotLight, tglSpotLight,
            lblMaterial, tglMaterial
        );

  }

  private long cumul = 0;
  private int nbPass = 0;

  @Override
  public void update(long tpf) {

    cumul += tpf;
    nbPass++;

    if (cumul > 1000) {
      this.lblElapsed.text("fps : %d".formatted(nbPass));
      cumul = 0;
      nbPass = 0;
    }

    this.builder.begin();
    this.lblElapsed.build();
    this.pnlConfiguration.build();
    this.builder.end();
  }

  @Override
  public void input(Input input, long tpf) {
    this.builder.input(input);
  }

}
