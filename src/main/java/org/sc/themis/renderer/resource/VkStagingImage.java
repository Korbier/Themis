package org.sc.themis.renderer.resource;

import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.base.device.VkMemoryAllocator;
import org.sc.themis.renderer.base.resource.image.VkImage;
import org.sc.themis.renderer.base.resource.image.VkImageDescriptor;
import org.sc.themis.renderer.base.resource.image.VkImageView;
import org.sc.themis.renderer.base.resource.image.VkImageViewDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.Image;
import org.sc.themis.shared.utils.MathUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.lwjgl.vulkan.VK10.*;

public final class VkStagingImage extends VkStagingResource {

  private final VkDevice device;
  private final int imageFormat;
  private final int layers;

  private Image master;
  private Image [] sources;
  private VkImage image;
  private VkImageView view;
  private int mipLevels;

  VkStagingImage(
      Configuration configuration,
      VkStagingResourceAllocator resourceAllocator,
      VkDevice device,
      VkMemoryAllocator allocator,
      int imageFormat,
      int layers
  ) {
    super(configuration, resourceAllocator, device, allocator);
    this.device = device;
    this.imageFormat = imageFormat;
    this.layers = layers;
  }

  @Override
  protected void setupStagingBuffer() throws ThemisException {
    super.setupStagingBuffer();
    setupMipLevels();
    setupImage();
    setupView();
  }

  @Override
  protected void cleanupStagingBuffer() throws ThemisException {
    this.view.cleanup();
    this.image.cleanup();
    super.cleanupStagingBuffer();
  }

  @Override
  public void doCommit(VkCommand command) throws ThemisException {
    command.layout(
        this.image,
        VK_IMAGE_LAYOUT_UNDEFINED,
        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
        VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
        VK_PIPELINE_STAGE_TRANSFER_BIT,
        0,
        VK_ACCESS_TRANSFER_WRITE_BIT,
        it ->
            it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                .levelCount(this.mipLevels)
                .baseArrayLayer(0)
                .layerCount(1));
    command.copy(getStagingBuffer(), this.image);
    command.generateMipMaps(this.image, this.mipLevels);
  }

  public VkImageView getView() {
    return this.view;
  }

  public void load(Image ... images) throws ThemisException {

    Assertions.isTrue(
        () -> this.layers == images.length,
        new ThemisException()
    );

    this.sources = images;
    this.master = this.sources[0];

    ByteBuffer [] buffers = new ByteBuffer[images.length];

    for (int i = 0; i < images.length; i++) {
      buffers[i] = images[i].getBuffer();
    }

    load(buffers);

  }

  private void setupMipLevels() {
    this.mipLevels =
        (int) Math.floor(MathUtils.log2(Math.min(this.master.getWidth(), this.master.getHeight())))
            + 1;
  }

  private void setupImage() throws ThemisException {
    VkImageDescriptor descriptor =
        new VkImageDescriptor(
            this.imageFormat, this.mipLevels,
            this.master.getWidth(), this.master.getHeight(),
            VK_SAMPLE_COUNT_1_BIT, this.layers,
            VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
            0);
    this.image = new VkImage(getConfiguration(), this.device, descriptor);
    this.image.setup();
  }

  private void setupView() throws ThemisException {
    VkImageViewDescriptor descriptor =
        new VkImageViewDescriptor(
            VK_IMAGE_ASPECT_COLOR_BIT,
            0,
            this.image.getDescriptor().format(),
            this.layers,
            this.mipLevels,
            VK_IMAGE_VIEW_TYPE_2D);
    this.view =
        new VkImageView(getConfiguration(), this.device, this.image.getHandle(), descriptor);
    this.view.setup();
  }

}
