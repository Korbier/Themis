package org.sc.themis.renderer.base.command.set;

import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.vkCmdBlitImage;

import java.util.function.Consumer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkOffset3D;
import org.sc.themis.renderer.base.command.VkCommandBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.image.VkImage;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public class ResourceSet extends VkCommandSet {

  Vulkan vulkan = new Vulkan();

  public ResourceSet(VkCommandBuffer buffer) {
    super(buffer);
  }

  public void copy(VkBuffer srcBuffer, VkBuffer dstBuffer, VkBufferCopyRegion ... regions)
      throws ThemisException {
    VkBufferCopy.Buffer copyRegion = createBufferCopy(regions);
   command.cmdCopyBuffer(buffer().getHandle(), srcBuffer.getHandle(), dstBuffer.getHandle(), copyRegion);
  }

  public void copy(VkBuffer srcBuffer, VkImage dstImage, VkBufferImageCopyRegion ... regions) throws ThemisException {
    VkBufferImageCopy.Buffer bufferImgCopy = createBufferImageCopy(regions);
   command.cmdCopyBufferToImage(
            buffer().getHandle(),
            srcBuffer.getHandle(),
            dstImage.getHandle(),
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            bufferImgCopy
    );
  }


  public void layout(
      VkImage image,
      int sourceLayout, int targetLayout,
      int srcPipelineStage, int dstPipelineStage,
      int srcAccessMask, int dstAccessMask,
      Consumer<VkImageSubresourceRange> subResourceRange
  ) throws ThemisException {
    VkImageMemoryBarrier.Buffer barrier =
        createImageMemoryBarrier(image.getHandle(), sourceLayout, targetLayout, srcAccessMask, dstAccessMask, subResourceRange);
   command.cmdPipelineBarrier(buffer().getHandle(), srcPipelineStage, dstPipelineStage, barrier);
  }

  public void generateMipMaps(VkImage image, int mipsLevel) throws ThemisException {

    int width = image.getDescriptor().width();
    int height = image.getDescriptor().height();

    for (int i = 1; i < mipsLevel; i++) {

      final int idx = i - 1;

      layout(
          image,
          VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
          VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT,
          VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT,
          it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).levelCount(1).layerCount(1).baseMipLevel(idx)
      );

      blit(image, i, width, height);

      layout(
          image,
          VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
          VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
          VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT,
          it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).levelCount(1).layerCount(1).baseMipLevel(idx)
      );

      if (width > 1) width /= 2;
      if (height > 1) height /= 2;
    }

    layout(
        image,
        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
        VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
        it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).levelCount(1).layerCount(1).baseMipLevel(mipsLevel - 1)
    );

  }

  public void blit(VkImage image, int mipLevel, int width, int height) {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkOffset3D srcOffset0 = VkOffset3D.calloc(stack).x(0).y(0).z(0);
      VkOffset3D srcOffset1 = VkOffset3D.calloc(stack).x(width).y(height).z(1);
      VkOffset3D dstOffset0 = VkOffset3D.calloc(stack).x(0).y(0).z(0);
      VkOffset3D dstOffset1 = VkOffset3D.calloc(stack).x(width > 1 ? width / 2 : 1).y(height > 1 ? height / 2 : 1).z(1);

      VkImageBlit.Buffer blit =
          VkImageBlit.calloc(1, stack)
              .srcOffsets(0, srcOffset0)
              .srcOffsets(1, srcOffset1)
              .srcSubresource(it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(mipLevel - 1).baseArrayLayer(0).layerCount(1))
              .dstOffsets(0, dstOffset0)
              .dstOffsets(1, dstOffset1)
              .dstSubresource(it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(mipLevel).baseArrayLayer(0).layerCount(1));

      vkCmdBlitImage(
          buffer().getHandle(),
          image.getHandle(), VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
          image.getHandle(), VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
          blit, VK_FILTER_LINEAR
      );

    }
  }

  private VkImageMemoryBarrier.Buffer createImageMemoryBarrier(
      long handle,
      int sourceLayout,
      int targetLayout,
      int srcAccessMask,
      int dstAccessMask,
      Consumer<VkImageSubresourceRange> subResourceRange
  ) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      return VkImageMemoryBarrier.calloc(1, stack)
          .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
          .image(handle)
          .oldLayout(sourceLayout).newLayout(targetLayout)
          .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED).dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
          .srcAccessMask(srcAccessMask).dstAccessMask(dstAccessMask)
          .subresourceRange(subResourceRange);
    }
  }

  private VkBufferCopy.Buffer createBufferCopy(VkBufferCopyRegion... regions) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkBufferCopy.Buffer buffers = VkBufferCopy.calloc(regions.length, stack);
      for (int i = 0; i < buffers.remaining(); i++) {
        buffers.get(i)
            .srcOffset(regions[i].srcOffset()).dstOffset(regions[i].dstOffset())
            .size(regions[i].size());
      }
      return buffers;
    }
  }

  private VkBufferImageCopy.Buffer createBufferImageCopy(VkBufferImageCopyRegion ... regions) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkBufferImageCopy.Buffer buffers = VkBufferImageCopy.calloc(regions.length, stack);
      for (int i = 0; i < buffers.remaining(); i++) {
        VkBufferImageCopyRegion region = regions[i];
        buffers.get(i)
            .bufferOffset(region.bufferOffset())
            .bufferRowLength(0)
            .bufferImageHeight(0)
            .imageSubresource( it ->
                    it.aspectMask(region.aspectMask())
                        .mipLevel(region.mipLevel())
                        .baseArrayLayer(region.baseArrayLayer())
                        .layerCount(region.layerCount())
            )
            .imageOffset(it -> it.x(0).y(0).z(0))
            .imageExtent(it -> it.width(region.imageWidth()).height(region.imageHeight()).depth(region.depth()));
      }
      return buffers;
    }
  }

}
