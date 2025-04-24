package org.sc.themis.renderer.lang.exception;

import org.lwjgl.vulkan.VK11;

public class VkOutOfPoolMemoryException extends VulkanException {

  public VkOutOfPoolMemoryException() {
    super(VK11.VK_ERROR_OUT_OF_POOL_MEMORY, "Out of pool memory");
  }
}
