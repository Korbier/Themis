package org.sc.viewer.gamestate;

import java.nio.file.Path;
import java.util.Optional;

import org.joml.Vector3f;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.resource.staging.VkStagingImage;
import org.sc.themis.scene.Scene;
import org.sc.themis.renderer.resource.model.Instance;
import org.sc.themis.renderer.resource.model.Model;
import org.sc.themis.scene.controller.OrbitCameraController;
import org.sc.themis.scene.factory.MaterialFactory;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.scene.light.PointLight;
import org.sc.themis.scene.light.SpotLight;
import org.sc.themis.scene.light.attenuation.Attenuation;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.renderer.resource.font.FontResourceDescriptor;
import org.sc.themis.renderer.resource.ResourceEnum;
import org.sc.themis.renderer.resource.ResourceLoader;
import org.sc.themis.renderer.resource.font.FontRepository;
import org.sc.themis.renderer.resource.model.ModelResourceDescriptor;
import org.sc.themis.renderer.resource.image.ImageResourceDescriptor;
import org.sc.viewer.ViewerContext;
import org.sc.viewer.gamestate.controller.KeyMappingController;
import org.sc.viewer.gamestate.controller.UiController;
import org.sc.viewer.renderactivity.geometry.material.TextureMaterialRenderer;
import org.sc.viewer.renderactivity.geometry.material.TextureWithNormalMappingMaterialRenderer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;

public class ViewerGamestate implements Gamestate {

  private final MaterialFactory materialFactory = new MaterialFactory();

  private final ViewerContext context;
  private final Pencil pencil;
  private Instance instance;
  private Model model;

  public ViewerGamestate(ViewerContext context) {
    this.context = context;

    FontRepository fontRepository = new FontRepository();

    try {
      fontRepository.load(ResourceLoader.get().get(ResourceEnum.FONT, FontResourceDescriptor.sdf( Path.of("CenturyGothic.ttf"), 14, 0.47f, 0.050f )));
      fontRepository.load(ResourceLoader.get().get(ResourceEnum.FONT, FontResourceDescriptor.sdf( Path.of("CenturyGothic.ttf"), 16, 0.46f, 0.09f )));
    } catch (ThemisException e) {
      e.printStackTrace(); //todo
    }

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
    this.context.getKeyMapping().map(GLFW_KEY_2, false, () -> {
      PointLight light = scene.getPointLights().getFirst();
      light.setVisible(!light.isVisible());
    });
    this.context.getKeyMapping().map(GLFW_KEY_3, false, () -> {
      SpotLight light = scene.getSpotLights().getFirst();
      light.setVisible(!light.isVisible());
    });
    this.context.getKeyMapping().map(GLFW_KEY_4, false, () -> {
      Optional<String> oMaterial = this.model.getMaterialRenderer();
      if (oMaterial.isEmpty() || !oMaterial.get().equals(TextureWithNormalMappingMaterialRenderer.MATERIAL_ID)) {
        this.model.setMaterial(TextureWithNormalMappingMaterialRenderer.MATERIAL_ID);
      } else {
        this.model.setMaterial(TextureMaterialRenderer.MATERIAL_ID);
      }
      System.out.println(this.model.getMaterialRenderer().get());


    });
    scene.add(new KeyMappingController(this.context.getKeyMapping()));
  }

  private void setupCamera(Scene scene) {
    scene.getCamera().setPosition(0.0f, 0.0f, 6f);
  }

  private void setupUI(Scene scene) {
    scene.add(new UiController(this.pencil, scene, this.context));
  }

  private void setupScene(Renderer renderer, Scene scene) throws ThemisException {

    VkStagingImage texture = renderer.getResourceAllocator().allocateImage(VK_FORMAT_R8G8B8A8_SRGB);
    texture.load(ResourceLoader.get().get(ResourceEnum.IMAGE, ImageResourceDescriptor.of("./src/main/resources/material/brickwall/brickwall_albedo.jpg"), Path.of(".")));

    VkStagingImage normalMap = renderer.getResourceAllocator().allocateImage(VK_FORMAT_R8G8B8A8_UNORM);
    normalMap.load(ResourceLoader.get().get(ResourceEnum.IMAGE, ImageResourceDescriptor.of("./src/main/resources/material/brickwall/brickwall_normal.jpg"), Path.of(".")));

    this.model = createSphere(renderer, "sphere-1");
    this.model.setMaterialProperties(this.materialFactory.textureWithNormalMap(texture, normalMap));
    this.model.setMaterial(TextureMaterialRenderer.MATERIAL_ID);

    this.instance = this.model.create().scale(1.5f); //.position(0.0f, -3.8f, 0.0f);//.position(0, -60.0f, -20.0f).scale(0.5f);
    scene.add(instance);
    //scene.add(new FpsCameraController(scene));
    scene.add(new OrbitCameraController(scene, instance));

    scene.add(
        new SpotLight(
            new Vector3f(0.0f, 0.0f, 0.01f),
            new Vector3f(0.0f, 0.0f, 0.7f),
            new Vector3f(0.0f, 0.0f, 0.9f),
            new Vector3f(0.0f, 0.0f, 10.0f),
            new Vector3f(0.0f, 0.0f, -10.0f),
            Attenuation.type1(128.0f, 128.0f),
            (float) Math.cos(Math.toRadians(12.0f)),
            (float) Math.cos(Math.toRadians(16.0f))));

    scene.add(
        new DirectionalLight(
            new Vector3f(0.01f),
            new Vector3f(0.5f),
            new Vector3f(0.7f),
            new Vector3f(0.0f, 0.0f, 15.0f)));

    scene.add(
        new PointLight(
            new Vector3f(0.01f),
            new Vector3f(0.7f, 0.0f, 0.0f),
            new Vector3f(0.9f, 0.0f, 0.0f),
            new Vector3f(0.0f, 0.0f, 3.0f), //new Vector3f(5.0f, d5.0f, 5.0f),
            Attenuation.type1(32.0f, 4.0f)));

    scene.getPointLights().getFirst().setVisible(false);
    scene.getSpotLights().getFirst().setVisible(false);


  }

  private Model createSphere(Renderer renderer, String id) throws ThemisException {
    return ResourceLoader.get().get(ResourceEnum.MODEL, ModelResourceDescriptor.of("base/textured_unit_cube.gltf", id, renderer.getResourceAllocator()));
  }

}
