package org.sc.themis.renderer.command.set;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.command.VkCommandBuffer;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.image.VkImage;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.function.Consumer;

import static org.lwjgl.vulkan.VK10.*;

public class ResourceSet extends VkCommandSet {

    public ResourceSet(Configuration configuration, VkCommandBuffer buffer) {
        super(configuration, buffer);
    }

    public void copy(VkBuffer srcBuffer, VkBuffer dstBuffer, Region ... regions) throws ThemisException {
        VkBufferCopy.Buffer copyRegion = createBufferCopy(  regions );
        vkCommand().cmdCopyBuffer( buffer().getHandle(), srcBuffer.getHandle(), dstBuffer.getHandle(), copyRegion );
    }

    public void copy( VkBuffer srcBuffer, VkImage dstImage ) throws ThemisException {
        VkBufferImageCopy.Buffer bufferImgCopy = createBufferImageCopy(dstImage.getDescriptor().width(), dstImage.getDescriptor().height());
        vkCommand().cmdCopyBufferToImage( buffer().getHandle(), srcBuffer.getHandle(), dstImage.getHandle(), VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, bufferImgCopy );
    }

    public void layout(VkImage image, int sourceLayout, int targetLayout, int srcPipelineStage, int dstPipelineStage, int srcAccessMask, int dstAccessMask, Consumer<VkImageSubresourceRange> subResourceRange ) throws ThemisException {
        VkImageMemoryBarrier.Buffer barrier = createImageMemoryBarrier( image.getHandle(), sourceLayout, targetLayout, srcAccessMask, dstAccessMask, subResourceRange );
        vkCommand().cmdPipelineBarrier( buffer().getHandle(), srcPipelineStage, dstPipelineStage, barrier);
    }

    public void generateMipMaps( VkImage image, int mipsLevel ) throws ThemisException {

        int width  = image.getDescriptor().width();
        int height = image.getDescriptor().height();

        for (int i = 1; i < mipsLevel; i++) {

            final int idx = i - 1;

            layout(
                image,
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT,
                VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT,
                it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).levelCount(1).layerCount(1).baseMipLevel( idx )
            );
            blit( image, i, width, height );
            layout(
                image,
                VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT,
                it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).levelCount(1).layerCount(1).baseMipLevel( idx )
            );

            if (width > 1) width /= 2;
            if (height > 1) height /= 2;

        }

        layout(
            image,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
            VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
            VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
            it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).levelCount(1).layerCount(1).baseMipLevel( mipsLevel - 1 )
        );

    }

    public void blit( VkImage image, int mipLevel, int width, int height ) {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkOffset3D srcOffset0 = VkOffset3D.calloc(stack).x(0).y(0).z(0);
            VkOffset3D srcOffset1 = VkOffset3D.calloc(stack).x(width).y(height).z(1);
            VkOffset3D dstOffset0 = VkOffset3D.calloc(stack).x(0).y(0).z(0);
            VkOffset3D dstOffset1 = VkOffset3D.calloc(stack).x(width > 1 ? width / 2 : 1).y(height > 1 ? height / 2 : 1).z(1);

            VkImageBlit.Buffer blit = VkImageBlit.calloc(1, stack)
                    .srcOffsets(0, srcOffset0)
                    .srcOffsets(1, srcOffset1)
                    .srcSubresource(it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(mipLevel-1).baseArrayLayer(0).layerCount(1))
                    .dstOffsets(0, dstOffset0)
                    .dstOffsets(1, dstOffset1)
                    .dstSubresource(it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(mipLevel).baseArrayLayer(0).layerCount(1));

            vkCmdBlitImage( buffer().getHandle(),
                image.getHandle(), VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                image.getHandle(), VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                blit, VK_FILTER_LINEAR
            );

        }

    }

    private VkBufferCopy.Buffer createBufferCopy(Region... regions) {
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            VkBufferCopy.Buffer buffers = VkBufferCopy.calloc(regions.length, stack);
            for (int i = 0; i < buffers.remaining(); i++) {
                buffers.get(i).srcOffset(regions[i].srcOffset).dstOffset(regions[i].dstOffset).size(regions[i].size);
            }
            return buffers;
        }
    }

    private VkImageMemoryBarrier.Buffer createImageMemoryBarrier(long handle, int sourceLayout, int targetLayout, int srcAccessMask, int dstAccessMask, Consumer<VkImageSubresourceRange> subResourceRange) {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            return VkImageMemoryBarrier.calloc(1, stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                    .oldLayout(sourceLayout)
                    .newLayout(targetLayout)
                    .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .image(handle)
                    .srcAccessMask(srcAccessMask)
                    .dstAccessMask(dstAccessMask)
                    .subresourceRange(subResourceRange);
        }
    }

    private VkBufferImageCopy.Buffer createBufferImageCopy(int width, int height) {
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            return VkBufferImageCopy.calloc(1, stack)
                    .bufferOffset(0)
                    .bufferRowLength(0)
                    .bufferImageHeight(0)
                    .imageSubresource(it ->
                            it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                                    .mipLevel(0)
                                    .baseArrayLayer(0)
                                    .layerCount(1)
                    )
                    .imageOffset(it -> it.x(0).y(0).z(0))
                    .imageExtent(it -> it.width(width).height(height).depth(1));
        }
    }

    public static class Region {

        private long srcOffset;
        private long dstOffset;
        private long size;

        public static Region of(long srcOffset, long dstOffset, long size ) {
            Region region = new Region();
            region.srcOffset = srcOffset;
            region.dstOffset = dstOffset;
            region.size = size;
            return region;
        }

        public long getSrcOffset() {
            return srcOffset;
        }

        public long getDstOffset() {
            return dstOffset;
        }

        public long getSize() {
            return size;
        }
    }

}
