package org.sc.themis.renderer.resource.font;

import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiFunctionWithException;

import java.nio.file.Path;

public class FontResourceLoader implements BiFunctionWithException<Path, FontResourceDescriptor, Font> {

  @Override
  public Font apply(Path path, FontResourceDescriptor descriptor) throws ThemisException {
    return
        descriptor.sdf()
        ? Font.sdf(descriptor.size(), descriptor.sdfWidth(), descriptor.sdfEdge(), path)
        : Font.normal(descriptor.size(), path);
  }

}
