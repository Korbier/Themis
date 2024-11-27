package org.sc.themis.renderer.resource;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;

public class VkImageView extends VulkanObject {

    private final VkDevice device;
    private final long imageHandle;
    private final VkImageViewDescriptor descriptor;

    private long handle;

    /**
    public VkImageView(Configuration configuration, VkDevice device, VkImage image, VkImageViewDescriptor descriptor ) {
        this( vk, device, image.getHandle(), descriptor );
    }
     **/

    public VkImageView(Configuration configuration, VkDevice device, long imageHandle, VkImageViewDescriptor descriptor ) {
        super(configuration);
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
        vkImage().destroyImageView( this.device.getHandle(), this.handle );
    }

    public long getHandle() {
        return this.handle;
    }

    public VkImageViewDescriptor getDescriptor() {
        return this.descriptor;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {handle=" + Long.toHexString( getHandle() ) + "}";
    }

    private VkImageViewCreateInfo createImageViewCreateInfo(MemoryStack stack) {
        return VkImageViewCreateInfo.calloc(stack)
            .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
            .image( this.imageHandle )
            .viewType( this.descriptor.viewType())
            .format( this.descriptor.format())
            .subresourceRange(it -> it
                    .aspectMask( this.descriptor.aspectMask() )
                    .baseMipLevel(0)
                    .levelCount( this.descriptor.mipLevels() )
                    .baseArrayLayer( this.descriptor.baseArrayLayer())
                    .layerCount( this.descriptor.layerCount())
            );
    }

    private long vkCreateImageView(MemoryStack stack, VkImageViewCreateInfo viewCreateInfo) throws ThemisException {
        LongBuffer lp = stack.mallocLong(1);
        vkImage().createImageView( this.device.getHandle(), viewCreateInfo, lp);
        return lp.get(0);
    }

}
