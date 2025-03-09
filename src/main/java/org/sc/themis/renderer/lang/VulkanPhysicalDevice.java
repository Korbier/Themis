package org.sc.themis.renderer.lang;

import static org.lwjgl.vulkan.VK10.vkEnumerateDeviceExtensionProperties;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFeatures;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;

import java.nio.IntBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.sc.themis.renderer.base.exception.VkInitializationFailedException;
import org.sc.themis.renderer.base.exception.VkLayerNotPresentException;
import org.sc.themis.renderer.base.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.base.exception.VkOutOfHostMemoryException;
import org.sc.themis.shared.exception.ThemisException;

public class VulkanPhysicalDevice extends Vulkan {

  public void enumerateDeviceExtensionProperties(
      VkPhysicalDevice physicalDevice,
      IntBuffer pPropertyCount,
      VkExtensionProperties.Buffer pProperties)
      throws ThemisException {
    vk(
        () ->
            vkEnumerateDeviceExtensionProperties(
                physicalDevice, (String) null, pPropertyCount, pProperties),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_LAYER_NOT_PRESENT)
            throw new VkLayerNotPresentException();
        });
  }

  public void enumeratePhysicalDevices(
      VkInstance instance, IntBuffer pPhysicalDeviceCount, PointerBuffer pPhysicalDevices)
      throws ThemisException {
    vk(
        () -> vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, pPhysicalDevices),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_INITIALIZATION_FAILED)
            throw new VkInitializationFailedException();
        });
  }

  public void getPhysicalDeviceMemoryProperties(
      VkPhysicalDevice physicalDevice, VkPhysicalDeviceMemoryProperties pMemoryProperties)
      throws ThemisException {
    vk(() -> vkGetPhysicalDeviceMemoryProperties(physicalDevice, pMemoryProperties));
  }

  public void getPhysicalDeviceFeatures(
      VkPhysicalDevice physicalDevice, VkPhysicalDeviceFeatures pFeatures) throws ThemisException {
    vk(() -> vkGetPhysicalDeviceFeatures(physicalDevice, pFeatures));
  }

  public void getPhysicalDeviceProperties(
      VkPhysicalDevice physicalDevice, VkPhysicalDeviceProperties pProperties)
      throws ThemisException {
    vk(() -> vkGetPhysicalDeviceProperties(physicalDevice, pProperties));
  }

  public void getPhysicalDeviceQueueFamilyProperties(
      VkPhysicalDevice physicalDevice,
      IntBuffer pQueueFamilyPropertyCount,
      VkQueueFamilyProperties.Buffer pQueueFamilyProperties)
      throws ThemisException {
    vk(
        () ->
            vkGetPhysicalDeviceQueueFamilyProperties(
                physicalDevice, pQueueFamilyPropertyCount, pQueueFamilyProperties));
  }
}
