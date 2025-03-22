package org.sc.themis.shared.resource.loader.descriptor;

import java.nio.file.Path;

public class TextureResourceDescriptor extends ResourceDescriptor {

  public static TextureResourceDescriptor of(String name) {
    return of(Path.of(name));
  }

  public static TextureResourceDescriptor of(Path name) {
    return new TextureResourceDescriptor(name);
  }

  public TextureResourceDescriptor(Path name) {
    super(name);
  }

}
