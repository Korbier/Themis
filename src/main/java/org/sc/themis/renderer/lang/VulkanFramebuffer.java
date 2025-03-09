package org.sc.themis.renderer.lang;

import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyFramebuffer;

import java.nio.LongBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.sc.themis.renderer.base.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.base.exception.VkOutOfHostMemoryException;
import org.sc.themis.shared.exception.ThemisException;

public class VulkanFramebuffer extends Vulkan {

  public void createFramebuffer(
      VkDevice device, VkFramebufferCreateInfo pCreateInfo, LongBuffer pFramebuffer)
      throws ThemisException {
    vk(
        () -> vkCreateFramebuffer(device, pCreateInfo, null, pFramebuffer),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroyFramebuffer(VkDevice device, long framebuffer) throws ThemisException {
    vk(() -> vkDestroyFramebuffer(device, framebuffer, null));
  }
}
