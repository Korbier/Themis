package org.sc.themis.renderer.framebuffer;

import org.lwjgl.vulkan.VkExtent2D;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class VkFrameBufferAttachments extends VulkanObject {

    private final VkDevice device;
    private final int width;
    private final int height;
    private final Map<String, VkFrameBufferAttachment> attachments = new LinkedHashMap<>();

    public VkFrameBufferAttachments(Configuration configuration, VkDevice device, int width, int height ) {
        super( configuration );
        this.device = device;
        this.width = width;
        this.height = height;
    }

    public VkFrameBufferAttachments(Configuration configuration, VkDevice device, VkExtent2D extent ) {
        super( configuration );
        this.device = device;
        this.width = extent.width();
        this.height = extent.height();
    }

    @Override
    public void setup() {
    }

    public int size() {
        return this.attachments.size();
    }

    @Override
    public void cleanup() throws ThemisException {
        for ( VkFrameBufferAttachment attachment : this.attachments.values() ) {
            attachment.cleanup();
        }
    }

    public VkFrameBufferAttachments color( String name, int format, int usage, int sampleCount ) throws ThemisException {
        VkFrameBufferAttachment attachment = VkFrameBufferAttachment.color( getConfiguration(), this.device, this.width, this.height, format, usage, sampleCount );
        attachment.setup();
        this.attachments.put( name, attachment );
        return this;
    }

    public VkFrameBufferAttachments depth( String name, int format, int usage ) throws ThemisException {
        return depth( name, format, usage, 1 );
    }

    public VkFrameBufferAttachments depth( String name, int format, int usage, int layers ) throws ThemisException {
        VkFrameBufferAttachment attachment = VkFrameBufferAttachment.depth( getConfiguration(), this.device, this.width, this.height, format, usage, layers );
        attachment.setup();
        this.attachments.put( name, attachment );
        return this;
    }

    public VkFrameBufferAttachments raw(String name, int format ) throws ThemisException {
        VkFrameBufferAttachment attachment = VkFrameBufferAttachment.raw( getConfiguration(), format );
        attachment.setup();
        this.attachments.put( name, attachment );
        return this;
    }

    public long getColorAttachmentCount() {
        return this.attachments.values().stream().filter( a -> a.getType() == VkFrameBufferAttachment.VkFrameBufferAttachmentType.COLOR || a.getType() == VkFrameBufferAttachment.VkFrameBufferAttachmentType.RAW ).count();
    }

    public long getImageCount() {
        return this.attachments.values().stream().filter( a -> a.getType() != VkFrameBufferAttachment.VkFrameBufferAttachmentType.RAW ).count();
    }

    public boolean hasDepthAttachment() {
        return this.attachments.values().stream().anyMatch( a -> a.getType() == VkFrameBufferAttachment.VkFrameBufferAttachmentType.DEPTH );
    }

    public VkFrameBufferAttachment get( String name ) {
        return this.attachments.get( name );
    }

    public Collection<VkFrameBufferAttachment> get() {
        return this.attachments.values();
    }


}
