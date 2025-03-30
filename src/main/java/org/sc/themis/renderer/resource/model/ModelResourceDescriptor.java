package org.sc.themis.renderer.resource.model;

import org.sc.themis.renderer.base.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.renderer.resource.base.ResourceDescriptor;

import java.nio.file.Path;

public class ModelResourceDescriptor extends ResourceDescriptor {

  private String identifier;
  private VkStagingResourceAllocator allocator;

  public static ModelResourceDescriptor of(String name, String identifier, VkStagingResourceAllocator allocator) {
    return of(Path.of(name), identifier, allocator);
  }

  public static ModelResourceDescriptor of(Path name, String identifier, VkStagingResourceAllocator allocator) {
    ModelResourceDescriptor descriptor = new ModelResourceDescriptor(name);
    descriptor.identifier = identifier;
    descriptor.allocator = allocator;
    return descriptor;
  }

  public ModelResourceDescriptor(Path name) {
    super(name);
  }

  public String identifier() {
    return identifier;
  }

  public VkStagingResourceAllocator allocator() {
    return allocator;
  }

}
