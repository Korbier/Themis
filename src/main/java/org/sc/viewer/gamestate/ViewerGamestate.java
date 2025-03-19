package org.sc.viewer.gamestate;

import java.nio.file.Path;
import org.joml.Vector3f;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.base.geometry.Model;
import org.sc.themis.scene.controller.FpsCameraController;
import org.sc.themis.scene.factory.MaterialFactory;
import org.sc.themis.scene.factory.ModelFactory;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.scene.light.PointLight;
import org.sc.themis.scene.light.SpotLight;
import org.sc.themis.scene.light.attenuation.Attenuation;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.font.Font;
import org.sc.themis.shared.resource.font.FontRepository;
import org.sc.viewer.ViewerContext;
import org.sc.viewer.gamestate.controller.KeyMappingController;
import org.sc.viewer.gamestate.controller.UiController;
import org.sc.viewer.renderactivity.geometry.material.ColorMaterial;

import static org.lwjgl.glfw.GLFW.*;

public class ViewerGamestate implements Gamestate {

  private final ModelFactory modelFactory = new ModelFactory();
  private final MaterialFactory materialFactory = new MaterialFactory();

  private final ViewerContext context;
  private final Pencil pencil;

  private Model model;

  public ViewerGamestate(ViewerContext context) {
    this.context = context;

    FontRepository fontRepository = new FontRepository();
    fontRepository.load(Font.sdf(14, 0.46f, 0.09f, Path.of("./src/main/resources/playground/font/CenturyGothic.ttf")));
    fontRepository.load(Font.sdf(18, 0.46f, 0.09f, Path.of("./src/main/resources/playground/font/CenturyGothic.ttf")));

    this.pencil = new Pencil(fontRepository);

  }

  @Override
  public void setup(Renderer renderer, Scene scene) throws ThemisException {
    setupCamera(scene);
    setupUI(scene);
    setupScene(renderer, scene);
    setupKeyMapping(scene);
  }

  @Override
  public void cleanup(Renderer renderer, Scene scene) throws ThemisException {
    this.model.cleanup();
  }

  public Pencil getPencil() {
    return this.pencil;
  }

  private void setupKeyMapping(Scene scene) {
    this.context.getKeyMapping().map(GLFW_KEY_1, false, () -> {
      DirectionalLight light = scene.getDirectionalLights().getFirst();
      light.setVisible(!light.isVisible());
    });
    scene.add(new KeyMappingController(this.context.getKeyMapping()));
  }

  private void setupCamera(Scene scene) {
    scene.getCamera().setPosition(0.0f, 1.0f, 8.0f);
    scene.add(new FpsCameraController(scene));
  }

  private void setupUI(Scene scene) {
    scene.add(new UiController(this.pencil, scene, this.context));
  }

  private void setupScene(Renderer renderer, Scene scene) throws ThemisException {

    this.model = createSphere(renderer, "sphere-1");
    this.model.setMaterialProperties(this.materialFactory.color(1.0f, 1.0f, 1.0f, 128.0f));
    this.model.setMaterial(ColorMaterial.IDENTIFIER);

    scene.add(this.model.create().position(-3.0f, 3.0f, 0.0f));
    scene.add(this.model.create().position(-3.0f, 0.0f, 0.0f));
    scene.add(this.model.create().position(-3.0f, -3.0f, 0.0f));

    scene.add(this.model.create().position(0.0f, 3.0f, 0.0f));
    scene.add(this.model.create().position(0.0f, -3.0f, 0.0f));

    scene.add(this.model.create().position(3.0f, 3.0f, 0.0f));
    scene.add(this.model.create().position(3.0f, 0.0f, 0.0f));
    scene.add(this.model.create().position(3.0f, -3.0f, 0.0f));

    scene.add(
        new SpotLight(
            new Vector3f(0.0f, 0.0f, 0.1f),
            new Vector3f(0.0f, 0.0f, 0.7f),
            new Vector3f(0.0f, 0.0f, 0.9f),
            new Vector3f(0.0f, 0.5f, 10.0f),
            new Vector3f(0.0f, 0.5f, -10.0f),
            Attenuation.type1(128.0f, 64.0f),
            (float) Math.cos(Math.toRadians(18.0f)),
            (float) Math.cos(Math.toRadians(20.0f))));

    scene.add(
        new DirectionalLight(
            new Vector3f(0.01f),
            new Vector3f(0.1f),
            new Vector3f(0.3f),
            new Vector3f(0.0f, 0.0f, -1.0f)));

    scene.add(
        new PointLight(
            new Vector3f(0.01f),
            new Vector3f(0.4f, 0.0f, 0.0f),
            new Vector3f(0.7f, 0.0f, 0.0f),
            new Vector3f(5.0f, 5.0f, 5.0f),
            Attenuation.type1(128.0f, 64.0f)));
  }

  private Model createSphere(Renderer renderer, String id) throws ThemisException {
    return this.modelFactory.create(
        id,
        renderer.getResourceAllocator(),
        Path.of("./src/main/resources/model/sphere/scene.gltf")
    );
  }
}
