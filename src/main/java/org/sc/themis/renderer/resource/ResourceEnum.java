package org.sc.themis.renderer.resource;

import org.sc.themis.renderer.resource.font.Font;
import org.sc.themis.renderer.resource.font.FontResourceLoader;
import org.sc.themis.renderer.resource.image.Image;
import org.sc.themis.renderer.resource.image.ImageResourceLoader;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.renderer.resource.material.MaterialResourceDescriptor;
import org.sc.themis.renderer.resource.material.MaterialResourceLoader;
import org.sc.themis.renderer.resource.model.Model;
import org.sc.themis.renderer.resource.model.ModelResourceLoader;
import org.sc.themis.renderer.resource.shader.ShaderSource;
import org.sc.themis.renderer.resource.shader.ShaderSourceResourceLoader;
import org.sc.themis.shared.function.BiFunctionWithException;
import org.sc.themis.renderer.resource.model.ModelResourceDescriptor;
import org.sc.themis.renderer.resource.shader.ShaderSourceResourceDescriptor;
import org.sc.themis.renderer.resource.font.FontResourceDescriptor;
import org.sc.themis.renderer.resource.image.ImageResourceDescriptor;
import java.nio.file.Path;

public class ResourceEnum {

  public final static BiFunctionWithException<Path, ImageResourceDescriptor, Image> imageLoader = new ImageResourceLoader();
  public final static BiFunctionWithException<Path, FontResourceDescriptor, Font>   fontLoader = new FontResourceLoader();
  public final static BiFunctionWithException<Path, ModelResourceDescriptor, Model> modelLoader = new ModelResourceLoader();
  public final static BiFunctionWithException<Path, ShaderSourceResourceDescriptor, ShaderSource> shaderLoader = new ShaderSourceResourceLoader();
  public final static BiFunctionWithException<Path, MaterialResourceDescriptor, Material> materialLoader = new MaterialResourceLoader();

  public final static ResourceType<Image, ImageResourceDescriptor> IMAGE = ResourceType.of(Image.class, Path.of("texture"), ResourceEnum.imageLoader, "png", "jpg" );
  public final static ResourceType<Font, FontResourceDescriptor> FONT = ResourceType.of(Font.class, Path.of("font"), ResourceEnum.fontLoader, "ttf");
  public final static ResourceType<Model, ModelResourceDescriptor> MODEL = ResourceType.of(Model.class, Path.of("model"), ResourceEnum.modelLoader, "obj", "gltf");
  public final static ResourceType<ShaderSource, ShaderSourceResourceDescriptor> SHADER = ResourceType.of(ShaderSource.class, Path.of("shader"), ResourceEnum.shaderLoader, "glsl" );
  public final static ResourceType<Material, MaterialResourceDescriptor> MATERIAL = ResourceType.of(Material.class, Path.of("material"), ResourceEnum.materialLoader, "json" );

}
