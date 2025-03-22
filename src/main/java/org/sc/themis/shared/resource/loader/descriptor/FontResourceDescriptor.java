package org.sc.themis.shared.resource.loader.descriptor;

import java.nio.file.Path;

public class FontResourceDescriptor extends ResourceDescriptor {

  private int size;
  private boolean sdf;
  private float sdfWidth;
  private float sdfEdge;

  public static FontResourceDescriptor sdf(Path name, int size, float sdfWidth, float sdfEdge) {
    FontResourceDescriptor descriptor = new FontResourceDescriptor(name);
    descriptor.size = size;
    descriptor.sdf = true;
    descriptor.sdfWidth = sdfWidth;
    descriptor.sdfEdge = sdfEdge;
    return descriptor;
  }

  public static FontResourceDescriptor normal(Path name, int size) {
    FontResourceDescriptor descriptor = new FontResourceDescriptor(name);
    descriptor.size = size;
    descriptor.sdf = false;
    return descriptor;
  }

  public FontResourceDescriptor(Path name) {
    super(name);
  }

  public int size() {
    return size;
  }

  public boolean sdf() {
    return sdf;
  }

  public float sdfWidth() {
    return sdfWidth;
  }

  public float sdfEdge() {
    return sdfEdge;
  }
}
