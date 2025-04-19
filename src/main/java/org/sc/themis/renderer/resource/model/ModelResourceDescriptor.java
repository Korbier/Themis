package org.sc.themis.renderer.resource.model;

import org.sc.themis.renderer.base.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.renderer.material.MaterialManager;
import org.sc.themis.renderer.resource.base.ResourceDescriptor;

import java.nio.file.Path;

public class ModelResourceDescriptor extends ResourceDescriptor {

  private String identifier;
  private VkStagingResourceAllocator allocator;
  private MaterialManager materialManager;

  public static ModelResourceDescriptor of(String name, String identifier, VkStagingResourceAllocator allocator, MaterialManager materialManager) {
    return of(Path.of(name), identifier, allocator, materialManager);
  }

  public static ModelResourceDescriptor of(Path name, String identifier, VkStagingResourceAllocator allocator, MaterialManager materialManager) {
    ModelResourceDescriptor descriptor = new ModelResourceDescriptor(name);
    descriptor.identifier = identifier;
    descriptor.allocator = allocator;
    descriptor.materialManager = materialManager;
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

  public MaterialManager materialManager() {
    return materialManager;
  }
}
