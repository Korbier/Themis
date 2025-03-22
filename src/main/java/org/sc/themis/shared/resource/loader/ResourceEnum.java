package org.sc.themis.shared.resource.loader;

import org.sc.themis.scene.base.geometry.Model;
import org.sc.themis.scene.factory.ModelFactory;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiFunctionWithException;
import org.sc.themis.shared.resource.Image;
import org.sc.themis.shared.resource.loader.descriptor.ModelResourceDescriptor;
import org.sc.themis.shared.resource.loader.descriptor.ShaderResourceDescriptor;
import org.sc.themis.shared.resource.loader.descriptor.FontResourceDescriptor;
import org.sc.themis.shared.resource.loader.descriptor.TextureResourceDescriptor;
import org.sc.themis.shared.resource.Font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceEnum {

  public final static BiFunctionWithException<Path, TextureResourceDescriptor, Image> imageLoader = (p, _) -> Image.of( p);

  public final static BiFunctionWithException<Path, ShaderResourceDescriptor, byte[]> shaderLoader = (p, _) -> {
    try {
      return Files.readAllBytes(p);
    } catch (IOException e) {
      throw new ThemisException(e.getMessage(), e);
    }
  };

  public final static BiFunctionWithException<Path, FontResourceDescriptor, Font> fontLoader = (p, d) -> d.sdf()
        ? Font.sdf(d.size(), d.sdfWidth(), d.sdfEdge(), p)
        : Font.normal(d.size(), p);

  public final static BiFunctionWithException<Path, ModelResourceDescriptor, Model> modelLoader = (p, d) -> ModelFactory.create(d.identifier(), d.allocator(), p);

  public final static ResourceType<Image, TextureResourceDescriptor> TEXTURE = ResourceType.of(Image.class, Path.of("texture"), ResourceEnum.imageLoader, "png", "jpg" );
  public final static ResourceType<byte[], ShaderResourceDescriptor> SHADER = ResourceType.of(byte[].class, Path.of("shader"), ResourceEnum.shaderLoader, "glsl" );
  public final static ResourceType<Font, FontResourceDescriptor> FONT = ResourceType.of(Font.class, Path.of("font"), ResourceEnum.fontLoader, "ttf");
  public final static ResourceType<Model, ModelResourceDescriptor> MODEL = ResourceType.of(Model.class, Path.of("model"), ResourceEnum.modelLoader, "obj", "gltf");

}
