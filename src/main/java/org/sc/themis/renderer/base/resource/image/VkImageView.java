package org.sc.themis.renderer.base.resource.image;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;

import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;

public class VkImageView extends Vulkan implements LifeCycle {

  private final VkDevice device;
  private final long imageHandle;
  private final VkImageViewDescriptor descriptor;

  private long handle;
  /**
   * public VkImageView(Configuration configuration, VkDevice device, VkImage image,
   * VkImageViewDescriptor descriptor ) { this( vk, device, image.getHandle(), descriptor ); }
   */
  public VkImageView(VkDevice device, long imageHandle, VkImageViewDescriptor descriptor) {
    this.device = device;
    this.imageHandle = imageHandle;
    this.descriptor = descriptor;
  }

  @Override
  public void setup() throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkImageViewCreateInfo viewCreateInfo = createImageViewCreateInfo(stack);
      this.handle = vkCreateImageView(stack, viewCreateInfo);
    }
  }

  @Override
  public void cleanup() throws ThemisException {
   image.destroyImageView(this.device.getHandle(), this.handle);
  }

  public long getHandle() {
    return this.handle;
  }

  public VkImageViewDescriptor getDescriptor() {
    return this.descriptor;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " {handle=" + Long.toHexString(getHandle()) + "}";
  }

  private VkImageViewCreateInfo createImageViewCreateInfo(MemoryStack stack) {
    return VkImageViewCreateInfo.calloc(stack)
        .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
        .image(this.imageHandle)
        .viewType(this.descriptor.viewType())
        .format(this.descriptor.format())
        .subresourceRange(
            it ->
                it.aspectMask(this.descriptor.aspectMask())
                    .baseMipLevel(0)
                    .levelCount(this.descriptor.mipLevels())
                    .baseArrayLayer(this.descriptor.baseArrayLayer())
                    .layerCount(this.descriptor.layerCount()));
  }

  private long vkCreateImageView(MemoryStack stack, VkImageViewCreateInfo viewCreateInfo)
      throws ThemisException {
    LongBuffer lp = stack.mallocLong(1);
   image.createImageView(this.device.getHandle(), viewCreateInfo, lp);
    return lp.get(0);
  }
}
