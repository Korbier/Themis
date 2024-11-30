package org.sc.themis.renderer.framebuffer;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;

public class VkFrameBuffer extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkFrameBuffer.class);

    private final VkDevice device;
    private final VkFrameBufferDescriptor descriptor;

    private long handle;

    public VkFrameBuffer(Configuration configuration, VkDevice device, VkFrameBufferDescriptor descriptor) {
        super( configuration );
        this.device = device;
        this.descriptor = descriptor;
    }

    @Override
    public void setup() throws ThemisException {


        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer imageViewBuffer = createImageViewBuffer(stack);
            VkFramebufferCreateInfo frameBufferCreateInfo = createFramebufferCreateInfo(stack, imageViewBuffer, descriptor.layers());
            this.handle = vkCreateFrameBuffer(stack, frameBufferCreateInfo);
        }

        LOG.trace( "VkFrameBuffer initialized" );
    }

    @Override
    public void cleanup() throws ThemisException {
        framebuffer().destroyFramebuffer( this.device.getHandle(), this.handle );
    }

    public long getHandle() {
        return this.handle;
    }

    public VkFrameBufferDescriptor getDescriptor() {
        return this.descriptor;
    }

    private long vkCreateFrameBuffer(MemoryStack stack, VkFramebufferCreateInfo frameBufferCreateInfo) throws ThemisException {
        LongBuffer pFramebuffer = stack.mallocLong(1);
        framebuffer().createFramebuffer( this.device.getHandle(), frameBufferCreateInfo, pFramebuffer );
        return pFramebuffer.get(0);
    }

    private LongBuffer createImageViewBuffer(MemoryStack stack) {

        LongBuffer attachmentsBuff = stack.mallocLong( this.descriptor.imageViews().length );

        for ( long imageView : this.descriptor.imageViews() ) {
            attachmentsBuff.put(imageView);
        }

        attachmentsBuff.flip();

        return attachmentsBuff;
    }

    private VkFramebufferCreateInfo createFramebufferCreateInfo( MemoryStack stack, LongBuffer imageViewBuffer, int layers) {
        return VkFramebufferCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                .pAttachments(imageViewBuffer)
                .width(this.descriptor.width())
                .height(this.descriptor.height())
                .layers(layers)
                .renderPass(this.descriptor.renderpass());
    }

}
