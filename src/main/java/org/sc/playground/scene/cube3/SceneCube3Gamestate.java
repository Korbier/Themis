package org.sc.playground.scene.cube3;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;

import org.joml.Vector4f;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.material.MaterialProperties;
import org.sc.themis.renderer.material.MaterialProperty;
import org.sc.themis.renderer.resource.staging.VkStagingImage;
import org.sc.themis.scene.Mesh;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.factory.MeshFactory;
import org.sc.themis.scene.factory.ModelFactory;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.Image;

public class SceneCube3Gamestate implements Gamestate {

  private final MeshFactory meshFactory = new MeshFactory();
  private final ModelFactory modelFactory = new ModelFactory();

  private VkStagingImage vkImage;

  private Model model;
  private Model model2;
  private Model model3;

  @Override
  public void setup(Renderer renderer, Scene scene) throws ThemisException {

    Image image = Image.of("src/main/resources/playground/descriptorset/imagesampler/vulkan.png");
    this.vkImage = renderer.getResourceAllocator().allocateImage(VK_FORMAT_R8G8B8A8_SRGB);
    this.vkImage.load(image);

    scene.getCamera().setPosition(0.0f, 0.0f, 7.0f);

    this.model = createCubeModel("cube1", renderer, new Vector4f(0.5f, 1.0f, 1.0f, 1.0f));
    scene.add(this.model.create());

    this.model2 = createCubeModel("cube2", renderer, new Vector4f(0.0f, 1.0f, 0.5f, 1.0f));
    scene.add(this.model2.create().position(4.0f, 0.0f, 0.0f).scale(0.5f));

    this.model3 = createCubeModel("cube3", renderer, new Vector4f(0.0f, 1.0f, 0.5f, 1.0f));
    scene.add(this.model3.create().position(-4.0f, 0.0f, 0.0f).rotate(45.0f, 1.0f, 0.0f, 0.0f));
  }

  @Override
  public void cleanup(Renderer renderer, Scene scene) throws ThemisException {
    this.model.cleanup();
    this.model2.cleanup();
    this.model3.cleanup();
    this.vkImage.cleanup();
  }

  @Override
  public void input(Scene scene, Input input, long tpf) {}

  @Override
  public void update(Scene scene, long tpf) {}

  private Model createCubeModel(String prefix, Renderer renderer, Vector4f color)
      throws ThemisException {

    MaterialProperties material = new MaterialProperties();
    material.put(MaterialProperty.Texture.BASE, vkImage);

    Mesh cube = this.meshFactory.createCube(renderer.getResourceAllocator(), prefix + "my-cube ");
    cube.setProperties(material);

    return this.modelFactory.create(prefix + "my-cube-model", cube);
  }
}
