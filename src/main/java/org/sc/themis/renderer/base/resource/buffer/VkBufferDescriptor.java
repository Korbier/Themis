package org.sc.themis.renderer.base.resource.buffer;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;

public record VkBufferDescriptor(
    boolean isAligned,
    long size, // the size in bytes of the buffer to be created.
    long chunckSize,
    int chunckCount,
    int bufferUsage, // a bitmask of VkBufferUsageFlagBits specifying allowed usages of the
    // buffer.
    int memoryUsage,
    int requiredFlags // an index identifying a memory type from the memoryTypes array of the
    // VkPhysicalDeviceMemoryProperties structure.
    ) {

  public static VkBufferDescriptor descriptorsetUniform(long size) {
    return new VkBufferDescriptor(
        size, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
  }

  public static VkBufferDescriptor descriptorsetDynamicUniform(long chunckSize, int chunckCount) {
    return new VkBufferDescriptor(
        chunckSize,
        chunckCount,
        VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
        0);
  }

  public static VkBufferDescriptor descriptorsetStorageBuffer(long size) {
    return new VkBufferDescriptor(
        size,
        VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
        VK_MEMORY_PROPERTY_HOST_CACHED_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
  }

  public static VkBufferDescriptor vertexBuffer(long size) {
    return new VkBufferDescriptor(
        size,
        VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
        VK_MEMORY_PROPERTY_HOST_CACHED_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
  }

  public static VkBufferDescriptor indiceBuffer(long size) {
    return new VkBufferDescriptor(
        size,
        VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
        VK_MEMORY_PROPERTY_HOST_CACHED_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
  }

  public VkBufferDescriptor(long size, int bufferUsage, int memoryUsage, int requiredFlags) {
    this(false, size, -1, -1, bufferUsage, memoryUsage, requiredFlags);
  }

  public VkBufferDescriptor(
      long chunckSize, int chunckCount, int bufferUsage, int memoryUsage, int requiredFlags) {
    this(true, -1, chunckSize, chunckCount, bufferUsage, memoryUsage, requiredFlags);
  }
}
