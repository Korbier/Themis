package org.sc.viewer.gamestate;

import org.joml.Vector3f;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.material.MaterialManager;
import org.sc.themis.renderer.resource.ResourceEnum;
import org.sc.themis.renderer.resource.ResourceLoader;
import org.sc.themis.renderer.resource.font.FontRepository;
import org.sc.themis.renderer.resource.font.FontResourceDescriptor;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.renderer.resource.material.MaterialResourceDescriptor;
import org.sc.themis.renderer.resource.model.Instance;
import org.sc.themis.renderer.resource.model.Model;
import org.sc.themis.renderer.resource.model.ModelResourceDescriptor;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.controller.OrbitCameraController;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.scene.light.PointLight;
import org.sc.themis.scene.light.SpotLight;
import org.sc.themis.scene.light.attenuation.Attenuation;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.ViewerContext;
import org.sc.viewer.gamestate.controller.KeyMappingController;
import org.sc.viewer.renderactivity.geometry.material.TextureMaterialRenderer;

import java.nio.file.Path;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;

public class ViewerGamestate implements Gamestate {

  private final ViewerContext context;
  private final MaterialManager materialManager;
  private final Pencil pencil;
  private Instance instance;
  private Model model;

  public ViewerGamestate(ViewerContext context, MaterialManager materialManager) {

    this.context = context;
    this.materialManager = materialManager;

    FontRepository fontRepository = new FontRepository();

    try {
      fontRepository.load(ResourceLoader.get().get(ResourceEnum.FONT, FontResourceDescriptor.sdf( Path.of("CenturyGothic.ttf"), 14, 0.47f, 0.060f )));
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
    /**
    this.context.getKeyMapping().map(GLFW_KEY_4, false, () -> {
      Optional<String> oMaterial = this.model.getMaterialRenderer();
      if (oMaterial.isEmpty() || !oMaterial.get().equals(TextureMaterialRenderer.MATERIAL_ID)) {
        this.model.setMaterialRenderer(TextureMaterialRenderer.MATERIAL_ID);
      } else {
        this.model.setMaterialRenderer(TextureMaterialRenderer.MATERIAL_ID);
      }
      System.out.println(this.model.getMaterialRenderer().get());
    });
     **/
    scene.add(new KeyMappingController(this.context.getKeyMapping()));
  }

  private void setupCamera(Scene scene) {
    scene.getCamera().setPosition(0.0f, 0.0f, 6f);
  }

  private void setupUI(Scene scene) {
    scene.add(new ViewerUi(scene, this.pencil, this.context));
  }

  private void setupScene(Renderer renderer, Scene scene) throws ThemisException {

    Material material = ResourceLoader.get().get(ResourceEnum.MATERIAL, MaterialResourceDescriptor.of("limestone3.json", renderer.getResourceAllocator()));
    this.materialManager.addMaterials(material);


    TextureMaterialRenderer materialRenderer = (TextureMaterialRenderer) materialManager.get(TextureMaterialRenderer.MATERIAL_ID);
    this.context.getKeyMapping().map(GLFW_KEY_4, false, materialRenderer::switchEnableNormal, materialRenderer::isNormalEnabled);

    this.model = ResourceLoader.get().get(ResourceEnum.MODEL, ModelResourceDescriptor.of("base/textured_unit_cube.gltf", "model", renderer.getResourceAllocator()));
    this.model.setMaterial(material);
    this.model.setMaterialRenderer(TextureMaterialRenderer.MATERIAL_ID);

    this.instance = this.model.create().scale(1.8f).position(1.0f, 0.0f, 0.0f);
    //anthro_shark = this.model.create().scale(1.0f).position(0.0f, -2.8f, 0.0f);
    //mechanic_projection_sub = this.model.create().scale(2.5f);
    //portrait_from_the_future = this.model.create().scale(0.5f).position(0.0f, -60.0f, -20.0f);
    scene.add(instance);
    //scene.add(new FpsCameraController(scene));
    scene.add(new OrbitCameraController(scene, instance));

    DirectionalLight dLight = new DirectionalLight(
        new Vector3f(0.01f),
        new Vector3f(0.5f),
        new Vector3f(0.7f),
        new Vector3f(0.0f, 0.0f, 5.0f)
    );
    scene.add(dLight);
    this.context.getKeyMapping().map(GLFW_KEY_1, false, dLight::switchVisible, dLight::isVisible);

    PointLight pLight = new PointLight(
        new Vector3f(0.01f),
        new Vector3f(0.7f, 0.0f, 0.0f),
        new Vector3f(0.9f, 0.0f, 0.0f),
        new Vector3f(0.0f, 5.0f, 3.0f), //new Vector3f(5.0f, d5.0f, 5.0f),
        Attenuation.type1(32.0f, 4.0f)
    );
    scene.add(pLight);
    this.context.getKeyMapping().map(GLFW_KEY_2, false, pLight::switchVisible, pLight::isVisible);

    SpotLight slight = new SpotLight(
        new Vector3f(0.0f, 0.0f, 0.01f),
        new Vector3f(0.0f, 0.0f, 0.8f),
        new Vector3f(0.0f, 0.0f, 0.9f),
        new Vector3f(0.0f, 0.0f, 10.0f),
        new Vector3f(0.0f, 0.0f, -10.0f),
        Attenuation.type1(512.0f, 256.0f),
        (float) Math.cos(Math.toRadians(4.0f)),
        (float) Math.cos(Math.toRadians(8.0f))
    );
    scene.add(slight);
    this.context.getKeyMapping().map(GLFW_KEY_3, false, slight::switchVisible, slight::isVisible);

  }

}
