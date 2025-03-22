package org.sc.themis.shared.resource.loader.descriptor;

import org.sc.themis.renderer.resource.VkStagingResourceAllocator;

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
