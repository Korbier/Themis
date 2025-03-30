package org.sc.themis.renderer.resource.shader;

import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiFunctionWithException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShaderSourceResourceLoader implements BiFunctionWithException<Path, ShaderSourceResourceDescriptor, ShaderSource> {

  @Override
  public ShaderSource apply(Path path, ShaderSourceResourceDescriptor descriptor) throws ThemisException {
    try {
      return new ShaderSource(Files.readAllBytes(path));
    } catch (IOException e) {
      throw new ThemisException(e.getMessage(), e);
    }
  }

}
