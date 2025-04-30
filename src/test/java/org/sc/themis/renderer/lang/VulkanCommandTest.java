package org.sc.themis.renderer.lang;

import static org.mockito.ArgumentMatchers.any;

import java.nio.LongBuffer;

import org.junit.jupiter.api.*;
import org.lwjgl.vulkan.VK10;
import org.sc.themis.renderer.lang.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.lang.exception.VkOutOfHostMemoryException;

public class VulkanCommandTest extends VulkanTest {

  private final VulkanCommand vulkanCommand = new VulkanCommand();

  @Test
  @DisplayName("CreateCommandPool - Success")
  public void testCreateCommandPool_success()  {
    mockVK10(
      vk10 -> vk10.when(() -> VK10.vkCreateCommandPool(any(), any(), any(), (LongBuffer) any())).thenReturn(VK10.VK_SUCCESS),
      () -> Assertions.assertDoesNotThrow(() -> vulkanCommand.createCommandPool(null, null, null))
    );
  }

  @Test
  @DisplayName("CreateCommandPool - VK_ERROR_OUT_OF_HOST_MEMORY")
  public void testCreateCommandPool_VK_ERROR_OUT_OF_HOST_MEMORY() {
    mockVK10(
      vk10 -> vk10.when(() -> VK10.vkCreateCommandPool(any(), any(), any(), (LongBuffer) any())).thenReturn(VK10.VK_ERROR_OUT_OF_HOST_MEMORY),
      () -> Assertions.assertThrows(VkOutOfHostMemoryException.class, () -> vulkanCommand.createCommandPool(null, null, null))
    );
  }

  @Test
  @DisplayName("CreateCommandPool - VK_ERROR_OUT_OF_DEVICE_MEMORY")
  public void testCreateCommandPool_VK_ERROR_OUT_OF_DEVICE_MEMORY() {
    mockVK10(
        vk10 -> vk10.when(() -> VK10.vkCreateCommandPool(any(), any(), any(), (LongBuffer) any())).thenReturn(VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY),
        () -> Assertions.assertThrows(VkOutOfDeviceMemoryException.class, () -> vulkanCommand.createCommandPool(null, null, null))
    );
  }

  @Test
  @DisplayName("AllocateCommandBuffers - success")
  public void testAllocateCommandBuffers_success() {
    mockVK10(
        vk10 -> vk10.when(() -> VK10.vkAllocateCommandBuffers(any(), any(), any())).thenReturn(VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY),
        () -> Assertions.assertThrows(VkOutOfDeviceMemoryException.class, () -> vulkanCommand.allocateCommandBuffers(null, null, null))
    );
  }

  @Test
  @DisplayName("AllocateCommandBuffers - VK_ERROR_OUT_OF_HOST_MEMORY")
  public void testAllocateCommandBuffers_VK_ERROR_OUT_OF_HOST_MEMORY() {
    mockVK10(
        vk10 -> vk10.when(() -> VK10.vkAllocateCommandBuffers(any(), any(), any())).thenReturn(VK10.VK_ERROR_OUT_OF_HOST_MEMORY),
        () -> Assertions.assertThrows(VkOutOfHostMemoryException.class, () -> vulkanCommand.allocateCommandBuffers(null, null, null))
    );
  }

  @Test
  @DisplayName("AllocateCommandBuffers - VK_ERROR_OUT_OF_DEVICE_MEMORY")
  public void testAllocateCommandBuffers_VK_ERROR_OUT_OF_DEVICE_MEMORY() {
    mockVK10(
        vk10 -> vk10.when(() -> VK10.vkAllocateCommandBuffers(any(), any(), any())).thenReturn(VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY),
        () -> Assertions.assertThrows(VkOutOfDeviceMemoryException.class, () -> vulkanCommand.allocateCommandBuffers(null, null, null))
    );
  }
}
