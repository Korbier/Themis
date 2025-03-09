package org.sc.themis.renderer.base;

import static org.lwjgl.vulkan.VK10.vkCreateFence;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroyFence;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;

import java.nio.LongBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.sc.themis.renderer.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.exception.VkOutOfHostMemoryException;
import org.sc.themis.shared.exception.ThemisException;

public class VulkanSync extends Vulkan {

  public void createSemaphore(
      VkDevice device, VkSemaphoreCreateInfo pCreateInfo, LongBuffer pSemaphore)
      throws ThemisException {
    vk(
        () -> vkCreateSemaphore(device, pCreateInfo, null, pSemaphore),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroySemaphore(VkDevice device, long semaphore) throws ThemisException {
    vk(() -> vkDestroySemaphore(device, semaphore, null));
  }

  public void createFence(VkDevice device, VkFenceCreateInfo pCreateInfo, LongBuffer pFence)
      throws ThemisException {
    vk(
        () -> vkCreateFence(device, pCreateInfo, null, pFence),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroyFence(VkDevice device, long fence) throws ThemisException {
    vk(() -> vkDestroyFence(device, fence, null));
  }

  public void waitForFence(VkDevice device, long fence) throws ThemisException {
    vk(() -> vkWaitForFences(device, fence, true, Long.MAX_VALUE));
  }

  public void resetFence(VkDevice device, long fence) throws ThemisException {
    vk(
        () -> vkResetFences(device, fence),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }
}
