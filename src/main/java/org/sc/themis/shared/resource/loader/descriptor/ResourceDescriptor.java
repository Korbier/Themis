package org.sc.themis.shared.resource.loader.descriptor;

import java.nio.file.Path;

public class ResourceDescriptor {

  private final Path name;

  public ResourceDescriptor(Path name) {
    this.name = name;
  }

  public Path name() {
    return name;
  }

}
