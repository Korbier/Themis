package org.sc.themis.renderer.pipeline.descriptorset;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.image.VkImage;
import org.sc.themis.renderer.resource.image.VkImageView;
import org.sc.themis.renderer.resource.image.VkSampler;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class VkDescriptorSet extends VulkanObject {

    private final VkDevice device;
    private final VkDescriptorPool descriptorPool;
    private final VkDescriptorSetLayout descriptorSetLayout;

    private long handle;

    public VkDescriptorSet(Configuration configuration, VkDevice device, VkDescriptorPool descriptorPool, VkDescriptorSetLayout descriptorSetLayout ) {
        super(configuration);
        this.device = device;
        this.descriptorPool = descriptorPool;
        this.descriptorSetLayout = descriptorSetLayout;
    }

    @Override
    public void setup() throws ThemisException {

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = createDescriptorSetAllocateInfo(stack);
            this.handle = vkCreateDescriptorSet(stack, descriptorSetAllocateInfo);
        }

    }

    @Override
    public void cleanup() throws ThemisException {
    }

    public long getHandle() {
        return this.handle;
    }

    private long vkCreateDescriptorSet(MemoryStack stack, VkDescriptorSetAllocateInfo descriptorSetAllocateInfo) throws ThemisException {
        LongBuffer pDescriptorSet = stack.mallocLong(1);
        vkPipeline().allocateDescriptorSets( this.device.getHandle(), descriptorSetAllocateInfo, pDescriptorSet);
        return pDescriptorSet.get(0);
    }

    private VkDescriptorSetAllocateInfo createDescriptorSetAllocateInfo(MemoryStack stack) {

        LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
        pDescriptorSetLayout.put( 0, this.descriptorSetLayout.getHandle());

        return VkDescriptorSetAllocateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                .descriptorPool(this.descriptorPool.getHandle())
                .pSetLayouts(pDescriptorSetLayout);

    }

    public void bind( int binding, VkBuffer buffer ) {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc(1, stack)
                    .buffer(buffer.getHandle())
                    .offset(0)
                    .range( buffer.isAligned() ? buffer.getAlignedSize() : buffer.getRequestedSize());

            VkWriteDescriptorSet.Buffer descrBuffer = VkWriteDescriptorSet.calloc(1, stack);

            descrBuffer.get(0)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstSet( getHandle() )
                    .dstBinding( this.descriptorSetLayout.getBinding( binding ).getBinding() )
                    .descriptorType( this.descriptorSetLayout.getBinding( binding ).getDescriptorType() )
                    .descriptorCount(1)
                    .pBufferInfo(bufferInfo);

            vkUpdateDescriptorSets(this.device.getHandle(), descrBuffer, null);

        }

    }

    public void bind( int binding, VkImageView imageView, VkSampler sampler ) {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack)
                    .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                    .imageView( imageView.getHandle() )
                    .sampler( sampler.getHandle() );

            VkWriteDescriptorSet.Buffer descrBuffer = VkWriteDescriptorSet.calloc(1, stack);
            descrBuffer.get(0)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstSet( getHandle() )
                    .dstBinding(binding)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .pImageInfo(imageInfo);

            vkUpdateDescriptorSets( this.device.getHandle(), descrBuffer, null);

        }

    }

}
