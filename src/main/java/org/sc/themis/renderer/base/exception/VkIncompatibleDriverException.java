package org.sc.themis.renderer.base.exception;

import org.lwjgl.vulkan.VK10;

public class VkIncompatibleDriverException extends VulkanException {

  public VkIncompatibleDriverException() {
    super(VK10.VK_ERROR_INCOMPATIBLE_DRIVER, "Incompatible driver");
  }
}
