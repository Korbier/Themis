package org.sc.themis.renderer.lang;

import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkDestroySampler;

import java.nio.LongBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.sc.themis.renderer.lang.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.lang.exception.VkOutOfHostMemoryException;
import org.sc.themis.shared.exception.ThemisException;

public class VulkanImage extends VulkanBackend {

  public void createImage(VkDevice device, VkImageCreateInfo pCreateInfo, LongBuffer pImage)
      throws ThemisException {
    vk(
        () -> vkCreateImage(device, pCreateInfo, null, pImage),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroyImage(VkDevice device, long image) throws ThemisException {
    vk(() -> vkDestroyImage(device, image, null));
  }

  public void createImageView(VkDevice device, VkImageViewCreateInfo pCreateInfo, LongBuffer pView)
      throws ThemisException {
    vk(
        () -> vkCreateImageView(device, pCreateInfo, null, pView),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroyImageView(VkDevice device, long imageView) throws ThemisException {
    vk(() -> vkDestroyImageView(device, imageView, null));
  }

  public void createSampler(VkDevice device, VkSamplerCreateInfo pCreateInfo, LongBuffer pSampler)
      throws ThemisException {
    vk(
        () -> vkCreateSampler(device, pCreateInfo, null, pSampler),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroySampler(VkDevice device, long sampler) throws ThemisException {
    vk(() -> vkDestroySampler(device, sampler, null));
  }
}
