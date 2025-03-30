package org.sc.themis.renderer.resource.image;

import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiFunctionWithException;

import java.nio.file.Path;

public class ImageResourceLoader implements BiFunctionWithException<Path, ImageResourceDescriptor, Image> {

  @Override
  public Image apply(Path path, ImageResourceDescriptor descriptor) throws ThemisException {
    return Image.of(path);
  }

}
