package org.sc.themis.renderer.base.sync;

import static org.lwjgl.vulkan.VK10.VK_FENCE_CREATE_SIGNALED_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;

import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;

public class VkFence extends Vulkan implements LifeCycle {

  private final VkDevice device;
  private final boolean signaled;

  private long handle;

  public VkFence(VkDevice device, boolean signaled) {
    this.device = device;
    this.signaled = signaled;
  }

  @Override
  public void setup() throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkFenceCreateInfo fenceCreateInfo = createFenceCreateInfo(stack);
      this.handle = this.vkCreateFence(stack, fenceCreateInfo);
    }
  }

  @Override
  public void cleanup() throws ThemisException {
   sync.destroyFence(this.device.getHandle(), this.getHandle());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " {handle=" + Long.toHexString(getHandle()) + "}";
  }

  public void waitForAndReset() throws ThemisException {
    waitFor();
    reset();
  }

  public long getHandle() {
    return this.handle;
  }

  private long vkCreateFence(MemoryStack stack, VkFenceCreateInfo fenceCreateInfo) throws ThemisException {
    LongBuffer lp = stack.mallocLong(1);
   sync.createFence(this.device.getHandle(), fenceCreateInfo, lp);
    return lp.get(0);
  }

  private VkFenceCreateInfo createFenceCreateInfo(MemoryStack stack) {
    return VkFenceCreateInfo.calloc(stack)
        .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
        .flags(this.signaled ? VK_FENCE_CREATE_SIGNALED_BIT : 0);
  }

  private void waitFor() throws ThemisException {
   sync.waitForFence(this.device.getHandle(), this.getHandle());
  }

  private void reset() throws ThemisException {
   sync.resetFence(this.device.getHandle(), this.getHandle());
  }
}
