package org.sc.themis.renderer.resource.model;

import org.sc.themis.scene.factory.ModelFactory;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiFunctionWithException;

import java.nio.file.Path;

public class ModelResourceLoader implements BiFunctionWithException<Path, ModelResourceDescriptor, Model> {

  @Override
  public Model apply(Path path, ModelResourceDescriptor descriptor) throws ThemisException {
    return ModelFactory.create(descriptor.identifier(), descriptor.allocator(), path);
  }

}
