package org.sc.themis.renderer.base.pipeline.descriptorset;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;

import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.base.framebuffer.VkFrameBufferAttachment;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.image.VkImageView;
import org.sc.themis.renderer.base.resource.image.VkSampler;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;

public class VkDescriptorSet extends Vulkan implements LifeCycle {

  private final VkDevice device;
  private final VkDescriptorPool descriptorPool;
  private final VkDescriptorSetLayout[] descriptorSetLayouts;

  private long handle;

  public VkDescriptorSet(
      VkDevice device,
      VkDescriptorPool descriptorPool,
      VkDescriptorSetLayout... descriptorSetLayouts) {
    this.device = device;
    this.descriptorPool = descriptorPool;
    this.descriptorSetLayouts = descriptorSetLayouts;
  }

  @Override
  public void setup() throws ThemisException {

    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkDescriptorSetAllocateInfo descriptorSetAllocateInfo =
          createDescriptorSetAllocateInfo(stack);
      this.handle = vkCreateDescriptorSet(stack, descriptorSetAllocateInfo);
    }
  }

  @Override
  public void cleanup() throws ThemisException {}

  public long getHandle() {
    return this.handle;
  }

  private long vkCreateDescriptorSet(MemoryStack stack, VkDescriptorSetAllocateInfo descriptorSetAllocateInfo) throws ThemisException {
    LongBuffer pDescriptorSet = stack.mallocLong(1);
   pipeline.allocateDescriptorSets(this.device.getHandle(), descriptorSetAllocateInfo, pDescriptorSet);
    return pDescriptorSet.get(0);
  }

  private VkDescriptorSetAllocateInfo createDescriptorSetAllocateInfo(MemoryStack stack) {

    LongBuffer pDescriptorSetLayout = stack.mallocLong(this.descriptorSetLayouts.length);
    int i = 0;

    for (VkDescriptorSetLayout layout : this.descriptorSetLayouts) {
      pDescriptorSetLayout.put(i++, layout.getHandle());
    }

    return VkDescriptorSetAllocateInfo.calloc(stack)
        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
        .descriptorPool(this.descriptorPool.getHandle())
        .pSetLayouts(pDescriptorSetLayout);
  }

  public void bind(int binding, VkBuffer buffer) throws ThemisException {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkDescriptorBufferInfo.Buffer bufferInfo =
          VkDescriptorBufferInfo.calloc(1, stack)
              .buffer(buffer.getHandle())
              .offset(0)
              .range(buffer.isAligned() ? buffer.getAlignedSize() : buffer.getRequestedSize());

      VkWriteDescriptorSet.Buffer descrBuffer =
          VkWriteDescriptorSet.calloc(this.descriptorSetLayouts.length, stack);

      int i = 0;

      for (VkDescriptorSetLayout layout : this.descriptorSetLayouts) {
        descrBuffer
            .get(0)
            .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
            .dstSet(getHandle())
            .dstBinding(layout.getBinding(binding).getBinding())
            .descriptorType(layout.getBinding(binding).getDescriptorType())
            .descriptorCount(1)
            .pBufferInfo(bufferInfo);
      }

     pipeline.updateDescriptorSets(this.device.getHandle(), descrBuffer, null);
    }
  }

  public void bind(int binding, VkImageView imageView, VkSampler sampler) throws ThemisException {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkDescriptorImageInfo.Buffer imageInfo =
          VkDescriptorImageInfo.calloc(1, stack)
              .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
              .imageView(imageView.getHandle())
              .sampler(sampler.getHandle());

      VkWriteDescriptorSet.Buffer descrBuffer = VkWriteDescriptorSet.calloc(1, stack);
      descrBuffer
          .get(0)
          .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
          .dstSet(getHandle())
          .dstBinding(binding)
          .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
          .descriptorCount(1)
          .pImageInfo(imageInfo);

     pipeline.updateDescriptorSets(this.device.getHandle(), descrBuffer, null);
    }
  }

  public void bind(int binding, VkFrameBufferAttachment attachment, VkSampler sampler)
      throws ThemisException {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkDescriptorImageInfo.Buffer imageInfo =
          VkDescriptorImageInfo.calloc(1, stack)
              .imageLayout(
                  attachment.getType() == VkFrameBufferAttachment.VkFrameBufferAttachmentType.DEPTH
                      ? VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL
                      : VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
              .sampler(sampler.getHandle())
              .imageView(attachment.getView().getHandle());

      VkWriteDescriptorSet.Buffer descrBuffer = VkWriteDescriptorSet.calloc(1, stack);
      descrBuffer
          .get(0)
          .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
          .dstSet(getHandle())
          .dstBinding(binding)
          .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
          .descriptorCount(1)
          .pImageInfo(imageInfo);

     pipeline.updateDescriptorSets(this.device.getHandle(), descrBuffer, null);
    }
  }

  public void bind(int binding, VkFrameBufferAttachment attachment) throws ThemisException {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkDescriptorImageInfo.Buffer imageInfo =
          VkDescriptorImageInfo.calloc(1, stack)
              .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
              .imageView(attachment.getView().getHandle());

      VkWriteDescriptorSet.Buffer descrBuffer = VkWriteDescriptorSet.calloc(1, stack);
      descrBuffer
          .get(0)
          .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
          .dstSet(getHandle())
          .dstBinding(binding)
          .descriptorType(VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT)
          .descriptorCount(1)
          .pImageInfo(imageInfo);

     pipeline.updateDescriptorSets(this.device.getHandle(), descrBuffer, null);
    }
  }
}
