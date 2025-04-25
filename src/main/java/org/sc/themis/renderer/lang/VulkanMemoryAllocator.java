package org.sc.themis.renderer.lang;

import static org.lwjgl.util.vma.Vma.vmaCreateAllocator;
import static org.lwjgl.util.vma.Vma.vmaCreateBuffer;
import static org.lwjgl.util.vma.Vma.vmaDestroyAllocator;
import static org.lwjgl.util.vma.Vma.vmaDestroyBuffer;
import static org.lwjgl.util.vma.Vma.vmaMapMemory;
import static org.lwjgl.util.vma.Vma.vmaUnmapMemory;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;

import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.sc.themis.renderer.lang.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.lang.exception.VkOutOfHostMemoryException;
import org.sc.themis.renderer.lang.exception.VulkanException;
import org.sc.themis.shared.exception.ThemisException;

public class VulkanMemoryAllocator extends VulkanBackend {

  public void createAllocator(VmaAllocatorCreateInfo createInfo, PointerBuffer pAllocator)
      throws ThemisException {
    vk(
        () -> vmaCreateAllocator(createInfo, pAllocator),
        (errno) -> {
          // TODO Check thrown exceptions
        });
  }

  public void destroyAllocator(long allocator) throws VulkanException {
    vk(() -> vmaDestroyAllocator(allocator));
  }

  public void createBuffer(
      long allocator,
      VkBufferCreateInfo bufferCreateInfo,
      VmaAllocationCreateInfo allocationCreateInfo,
      LongBuffer pBuffer,
      PointerBuffer pAllocation)
      throws ThemisException {
    vk(
        () ->
            vmaCreateBuffer(
                allocator, bufferCreateInfo, allocationCreateInfo, pBuffer, pAllocation, null),
        (errno) -> {
          // TODO Check thrown exceptions
        });
  }

  public void destroyBuffer(long allocator, long buffer, long allocation) throws ThemisException {
    vk(() -> vmaDestroyBuffer(allocator, buffer, allocation));
  }

  public void mapMemory(long allocator, long allocation, PointerBuffer pb) throws ThemisException {
    vk(
        () -> vmaMapMemory(allocator, allocation, pb),
        (errno) -> {
          // TODO Check thrown exceptions
        });
  }

  public void unmapMemory(long allocator, long allocation) throws ThemisException {
    vk(() -> vmaUnmapMemory(allocator, allocation));
  }

  public void getImageMemoryRequirements(
      VkDevice device, long image, VkMemoryRequirements pMemoryRequirements)
      throws ThemisException {
    vk(() -> vkGetImageMemoryRequirements(device, image, pMemoryRequirements));
  }

  public void allocateMemory(
      VkDevice device, VkMemoryAllocateInfo pAllocateInfo, LongBuffer pMemory)
      throws ThemisException {
    vk(
        () -> vkAllocateMemory(device, pAllocateInfo, null, pMemory),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void bindImageMemory(VkDevice device, long image, long memory, long memoryOffset)
      throws ThemisException {
    vk(
        () -> vkBindImageMemory(device, image, memory, memoryOffset),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void freeMemory(VkDevice device, long memory) throws ThemisException {
    vk(() -> vkFreeMemory(device, memory, null));
  }
}
