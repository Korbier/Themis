package org.sc.themis.renderer.resource.staging;

import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.device.VkMemoryAllocator;
import org.sc.themis.renderer.resource.image.VkImage;
import org.sc.themis.renderer.resource.image.VkImageDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.Image;
import org.sc.themis.shared.utils.MathUtils;

import static org.lwjgl.vulkan.VK10.*;

public final class VkStagingImage extends VkStagingResource {

    private final VkDevice device;
    private final int imageFormat;

    private Image source;
    private VkImage image;
    private int mipLevels;

    public VkStagingImage(Configuration configuration, VkDevice device, VkMemoryAllocator allocator, int imageFormat ) {
        super(configuration, device, allocator);
        this.device = device;
        this.imageFormat = imageFormat;
    }

    /**
     @Override
     public void doCommit(VkCommand command) throws ThemisException {

     }
     **/

    @Override
    protected void setupStagingBuffer() throws ThemisException {
        super.setupStagingBuffer();
        setupMipLevels();
        setupImage();
    }

    @Override
    protected void cleanupStagingBuffer() throws ThemisException {
        cleanupImage();
        super.cleanupStagingBuffer();
    }

    public void load( Image image ) throws ThemisException {
        this.source = image;
        load( image.getBuffer() );
    }

    private void setupMipLevels() {
        this.mipLevels = (int) Math.floor(MathUtils.log2(Math.min(this.source.getWidth(), this.source.getHeight()))) + 1;
    }

    private void setupImage() throws ThemisException {
        VkImageDescriptor descriptor = new VkImageDescriptor(
            this.imageFormat, this.mipLevels, this.source.getWidth(), this.source.getHeight(),
            VK_SAMPLE_COUNT_1_BIT, 1,
            VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
            0
        );
        this.image = new VkImage( getConfiguration(), this.device, descriptor );
        this.image.setup();
    }

    private void cleanupImage() throws ThemisException {
        this.image.cleanup();
    }

}
