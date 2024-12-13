package org.sc.themis.renderer.framebuffer;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.resource.image.VkImage;
import org.sc.themis.renderer.resource.image.VkImageDescriptor;
import org.sc.themis.renderer.resource.image.VkImageView;
import org.sc.themis.renderer.resource.image.VkImageViewDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import static org.lwjgl.vulkan.VK10.*;

public class VkFrameBufferAttachment extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkFrameBufferAttachment.class);

    public enum VkFrameBufferAttachmentType {
        COLOR,
        DEPTH,
        RAW
    }

    private final VkDevice device;
    private final int width;
    private final int height;
    private final int format;
    private final int sampleCount;
    private final VkFrameBufferAttachmentType type;
    private final int initialUsage;
    private final int layers;

    private VkImage image;
    private VkImageView imageView;

    public static VkFrameBufferAttachment color( Configuration configuration, VkDevice device, int width, int height, int format, int usage, int sampleCount ) {
        return new VkFrameBufferAttachment( configuration, device, VkFrameBufferAttachmentType.COLOR, width, height, format, usage, sampleCount, 1 );
    }

    public static VkFrameBufferAttachment depth( Configuration configuration,VkDevice device, int width, int height, int format, int usage ) {
        return new VkFrameBufferAttachment( configuration, device, VkFrameBufferAttachmentType.DEPTH, width, height, format, usage, VK_SAMPLE_COUNT_1_BIT, 1);
    }

    public static VkFrameBufferAttachment depth( Configuration configuration,VkDevice device, int width, int height, int format, int usage, int layers ) {
        return new VkFrameBufferAttachment( configuration, device, VkFrameBufferAttachmentType.DEPTH, width, height, format, usage, VK_SAMPLE_COUNT_1_BIT, layers );
    }

    public static VkFrameBufferAttachment raw(Configuration configuration, int format ) {
        return new VkFrameBufferAttachment( configuration, null, VkFrameBufferAttachmentType.RAW, -1, -1, format, -1, VK_SAMPLE_COUNT_1_BIT, 1 );
    }

    private VkFrameBufferAttachment(Configuration configuration, VkDevice device, VkFrameBufferAttachmentType type, int width, int height, int format, int usage, int sampleCount, int layers ) {
        super(configuration); //No Vulkan layer needed
        this.device = device;
        this.type = type;
        this.width = width;
        this.height = height;
        this.format = format;
        this.initialUsage = usage;
        this.sampleCount = sampleCount;
        this.layers = layers;
    }

    @Override
    public void setup() throws ThemisException {

        LOG.tracef( "FrameBufferAttachment setup (type = %s).", this.type);

        if (this.type != VkFrameBufferAttachmentType.RAW) {
            int mask = calculateMask();
            int usage = calculateUsage(this.initialUsage);
            setupImage( usage );
            setupImageView( mask );
        }

    }

    @Override
    public void cleanup() throws ThemisException {
        if ( this.imageView != null ) this.imageView.cleanup();
        if ( this.image != null ) this.image.cleanup();
    }

    public int getFormat() {
        return this.format;
    }

    public VkFrameBufferAttachmentType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {" +
                "type=" + type +
                ", format=" + format +
                ", height=" + height +
                ", width=" + width +
                ", image=" +  Long.toHexString( this.image.getHandle() ) +
                ", view=" +  Long.toHexString( this.imageView.getHandle() ) +
                '}';
    }

    public VkImage getImage() {
        return this.image;
    }

    private int calculateUsage( int initialUsage ) {

        int usage = initialUsage | VK_IMAGE_USAGE_SAMPLED_BIT;

        if ( this.type == VkFrameBufferAttachmentType.COLOR ) {
            usage |= VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
        } else if ( this.type == VkFrameBufferAttachmentType.DEPTH ) {
            usage |= VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
        }

        return usage;

    }

    private int calculateMask() {

        int aspectMask = 0;

        if ( this.type == VkFrameBufferAttachmentType.COLOR ) {
            aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        } else if ( this.type == VkFrameBufferAttachmentType.DEPTH ) {
            aspectMask = VK_IMAGE_ASPECT_DEPTH_BIT;
        }

        return aspectMask;

    }

    private VkImageDescriptor createImageDescriptor( int usage ) {
        return new VkImageDescriptor( this.format, 1, this.width, this.height, this.sampleCount, this.layers, usage, 0 );
    }

    private void setupImage(int usage) throws ThemisException {
        VkImageDescriptor imageDescriptor = createImageDescriptor(usage);
        this.image = new VkImage(getConfiguration(), this.device, imageDescriptor);
        image.setup();
    }

    private VkImageViewDescriptor createViewImageDescriptor( int mask ) {
        return new VkImageViewDescriptor( mask, 0, this.format, this.layers, 1, this.layers > 1 ? VK_IMAGE_VIEW_TYPE_2D_ARRAY : VK_IMAGE_VIEW_TYPE_2D);
    }

    private void setupImageView(int mask) throws ThemisException {
        VkImageViewDescriptor viewDescriptor = createViewImageDescriptor(mask);
        this.imageView = new VkImageView( getConfiguration(), this.device, image.getHandle(), viewDescriptor);
        this.imageView.setup();
    }

}
