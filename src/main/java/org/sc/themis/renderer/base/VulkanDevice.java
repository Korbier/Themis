package org.sc.themis.renderer.base;

import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.sc.themis.renderer.exception.VkDeviceLostException;
import org.sc.themis.renderer.exception.VkExtensionNotPresentException;
import org.sc.themis.renderer.exception.VkFeatureNotPresentException;
import org.sc.themis.renderer.exception.VkInitializationFailedException;
import org.sc.themis.renderer.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.exception.VkOutOfHostMemoryException;
import org.sc.themis.renderer.exception.VkTooManyObjectsException;
import org.sc.themis.shared.exception.ThemisException;

public class VulkanDevice extends Vulkan {

  public void createDevice(
      VkPhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, PointerBuffer pDevice)
      throws ThemisException {
    vk(
        () -> vkCreateDevice(physicalDevice, pCreateInfo, null, pDevice),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_INITIALIZATION_FAILED)
            throw new VkInitializationFailedException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_EXTENSION_NOT_PRESENT)
            throw new VkExtensionNotPresentException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_FEATURE_NOT_PRESENT)
            throw new VkFeatureNotPresentException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_TOO_MANY_OBJECTS)
            throw new VkTooManyObjectsException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_DEVICE_LOST)
            throw new VkDeviceLostException();
        });
  }

  public void destroyDevice(VkDevice device) throws ThemisException {
    vk(() -> vkDestroyDevice(device, null));
  }

  public void deviceWaitIdle(VkDevice device) throws ThemisException {
    vk(() -> vkDeviceWaitIdle(device));
  }

  public void getDeviceQueue(
      VkDevice device, int queueFamilyIndex, int queueIndex, PointerBuffer pQueue)
      throws ThemisException {
    vk(() -> vkGetDeviceQueue(device, queueFamilyIndex, queueIndex, pQueue));
  }
}
