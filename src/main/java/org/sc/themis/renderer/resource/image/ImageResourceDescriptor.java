package org.sc.themis.renderer.resource.image;

import org.sc.themis.renderer.resource.base.ResourceDescriptor;

import java.nio.file.Path;

public class ImageResourceDescriptor extends ResourceDescriptor {

  public static ImageResourceDescriptor of(String name) {
    return of(Path.of(name));
  }

  public static ImageResourceDescriptor of(Path name) {
    return new ImageResourceDescriptor(name);
  }

  public ImageResourceDescriptor(Path name) {
    super(name);
  }

}
