package org.sc.themis.renderer.base.command;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.sc.themis.core.LifeCycle;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.base.queue.VkQueue;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.shared.exception.ThemisException;
import org.slf4j.LoggerFactory;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;

public class VkCommandPool extends Vulkan implements LifeCycle {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VkCommandPool.class);

  private final VkDevice device;
  private final VkQueue queue;
  private long handle;

  public VkCommandPool(VkDevice device, VkQueue queue) {
    this.device = device;
    this.queue = queue;
  }

  @Override
  public void setup() throws ThemisException {

    try (MemoryStack stack = MemoryStack.stackPush()) {
      this.handle = this.vkCreateCommandPool(stack);
      logger.trace("CommandPool initialized.");
    }

  }

  @Override
  public void cleanup() throws ThemisException {
    vkDestroyCommandPool();
  }

  public long getHandle() {
    return this.handle;
  }

  public VkCommand create(boolean primary) throws ThemisException {

    VkCommandBuffer buffer = new VkCommandBuffer(this.device, this, this.queue, primary);
    buffer.setup();

    return new VkCommand(buffer);
  }

  private long vkCreateCommandPool(MemoryStack stack) throws ThemisException {

    VkCommandPoolCreateInfo cmdPoolInfo = VkCommandPoolCreateInfo
        .calloc(stack)
        .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
        .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
        .queueFamilyIndex(this.queue.getQueueFamilyIndex());

    LongBuffer lp = stack.mallocLong(1);

   command.createCommandPool(this.device.getHandle(), cmdPoolInfo, lp);

    return lp.get(0);

  }

  private void vkDestroyCommandPool() throws ThemisException {
   command.destroyCommandPool(this.device.getHandle(), this.handle);
  }
}
