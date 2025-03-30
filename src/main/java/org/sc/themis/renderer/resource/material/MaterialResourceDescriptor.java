package org.sc.themis.renderer.resource.material;

import org.sc.themis.renderer.base.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.renderer.resource.base.ResourceDescriptor;

import java.nio.file.Path;

public class MaterialResourceDescriptor extends ResourceDescriptor {

  private VkStagingResourceAllocator allocator;

  public static MaterialResourceDescriptor of(String name, VkStagingResourceAllocator allocator) {
    return of(Path.of(name), allocator);
  }

  public static MaterialResourceDescriptor of(Path name, VkStagingResourceAllocator allocator) {
    return new MaterialResourceDescriptor(name, allocator);
  }

  public MaterialResourceDescriptor(Path name, VkStagingResourceAllocator allocator) {
    super(name);
    this.allocator = allocator;
  }

  public VkStagingResourceAllocator getAllocator() {
    return this.allocator;
  }

}
