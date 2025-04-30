package org.sc.themis.renderer.base.device;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;
import org.sc.themis.shared.utils.LogUtils;
import org.slf4j.LoggerFactory;

public class VkMemoryAllocator extends Vulkan implements LifeCycle {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VkMemoryAllocator.class);

  private final VkInstance instance;
  private final VkDevice device;
  private final VkPhysicalDevice physicalDevice;

  private long handle;

  public VkMemoryAllocator(VkPhysicalDevice physicalDevice, VkDevice device, VkInstance instance) {
    this.physicalDevice = physicalDevice;
    this.device = device;
    this.instance = instance;
  }

  @Override
  public void setup() throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VmaAllocatorCreateInfo allocatorCreateInfo = createAllocatorCreateInfo(stack);
      this.handle = createMemoryAllocator(stack, allocatorCreateInfo);
      logger.trace("Memory allocator initialized (handle={})", LogUtils.toHexString(this.handle));
    }
  }

  @Override
  public void cleanup() throws ThemisException {
   memoryAllocator.destroyAllocator(this.handle);
  }

  public long getHandle() {
    return this.handle;
  }

  private long createMemoryAllocator(MemoryStack stack, VmaAllocatorCreateInfo allocatorCreateInfo) throws ThemisException {
    PointerBuffer pAllocator = stack.mallocPointer(1);
   memoryAllocator.createAllocator(allocatorCreateInfo, pAllocator);
    return pAllocator.get(0);
  }

  private VmaAllocatorCreateInfo createAllocatorCreateInfo(MemoryStack stack) {

    VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.calloc(stack).set(this.instance.getHandle(), this.device.getHandle());

    return VmaAllocatorCreateInfo.calloc(stack)
        .instance(this.instance.getHandle())
        .device(this.device.getHandle())
        .physicalDevice(this.physicalDevice.getHandle())
        .pVulkanFunctions(vmaVulkanFunctions);

  }
}
