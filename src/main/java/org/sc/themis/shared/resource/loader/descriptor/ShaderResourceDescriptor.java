package org.sc.themis.shared.resource.loader.descriptor;

import java.nio.file.Path;

public class ShaderResourceDescriptor extends ResourceDescriptor {

  public static ShaderResourceDescriptor of(String name) {
    return of(Path.of(name));
  }

  public static ShaderResourceDescriptor of(Path name) {
    return new ShaderResourceDescriptor(name);
  }

  public ShaderResourceDescriptor(Path name) {
    super(name);
  }

}
