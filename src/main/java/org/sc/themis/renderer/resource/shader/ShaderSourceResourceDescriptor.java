package org.sc.themis.renderer.resource.shader;

import org.sc.themis.renderer.resource.base.ResourceDescriptor;

import java.nio.file.Path;

public class ShaderSourceResourceDescriptor extends ResourceDescriptor {

  public static ShaderSourceResourceDescriptor of(String name) {
    return of(Path.of(name));
  }

  public static ShaderSourceResourceDescriptor of(Path name) {
    return new ShaderSourceResourceDescriptor(name);
  }

  public ShaderSourceResourceDescriptor(Path name) {
    super(name);
  }

}
