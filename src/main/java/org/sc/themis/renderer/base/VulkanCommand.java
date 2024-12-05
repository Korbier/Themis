package org.sc.themis.renderer.base;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.exception.*;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanCommand extends Vulkan {

    public void createCommandPool(VkDevice device, VkCommandPoolCreateInfo pCreateInfo, LongBuffer pCommandPool) throws ThemisException {
        vk(
            () -> vkCreateCommandPool( device, pCreateInfo, null, pCommandPool),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void destroyCommandPool(VkDevice device, long commandPool) throws ThemisException {
        vk( () -> vkDestroyCommandPool(device, commandPool, null) );
    }

    public void allocateCommandBuffers(VkDevice device, VkCommandBufferAllocateInfo pAllocateInfo, PointerBuffer pCommandBuffers) throws ThemisException {
        vk(
            () -> vkAllocateCommandBuffers( device, pAllocateInfo, pCommandBuffers),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void freeCommandBuffers(VkDevice device, long commandPool, VkCommandBuffer pCommandBuffers) throws ThemisException {
        vk( () -> vkFreeCommandBuffers( device, commandPool, pCommandBuffers ) );
    }

    public void beginCommandBuffer(VkCommandBuffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo) throws ThemisException {
        vk(
            () -> vkBeginCommandBuffer( commandBuffer, pBeginInfo ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void endCommandBuffer(VkCommandBuffer commandBuffer) throws ThemisException {
        vk(
                () -> vkEndCommandBuffer( commandBuffer ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void resetCommandBuffer(VkCommandBuffer commandBuffer) throws ThemisException {
        vk( () -> vkResetCommandBuffer( commandBuffer, VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT ) );
    }

    public void queueSubmit(VkQueue queue, VkSubmitInfo.Buffer pSubmits, long fence) throws ThemisException {
        vk(
            () -> vkQueueSubmit( queue, pSubmits, fence ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                if ( errno == VK_ERROR_DEVICE_LOST ) throw new VkDeviceLostException();
            }
        );
    }

    public void queueSubmit(VkQueue queue, VkSubmitInfo pSubmit, long fence) throws ThemisException {
        vk(
            () -> vkQueueSubmit( queue, pSubmit, fence ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                if ( errno == VK_ERROR_DEVICE_LOST ) throw new VkDeviceLostException();
            }
        );
    }

    public void cmdBeginRenderPass(VkCommandBuffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, int contents) throws ThemisException {
        vk( () -> vkCmdBeginRenderPass( commandBuffer, pRenderPassBegin, contents ) );
    }

    public void cmdEndRenderPass(VkCommandBuffer commandBuffer) throws ThemisException {
        vk( () -> vkCmdEndRenderPass( commandBuffer ) );
    }

    public void cmdSetViewport(VkCommandBuffer commandBuffer, VkViewport.Buffer viewports) throws ThemisException {
        vk( () -> vkCmdSetViewport( commandBuffer, 0, viewports ) );
    }

    public void cmdSetScissor(VkCommandBuffer commandBuffer, VkRect2D.Buffer scissors) throws ThemisException {
        vk( () -> vkCmdSetScissor( commandBuffer, 0, scissors ) );
    }

    public void cmdNextSubpass(VkCommandBuffer commandBuffer, int contents) throws ThemisException {
        vk( () -> vkCmdNextSubpass( commandBuffer, contents ) );
    }

    public void cmdBindVertexBuffers(VkCommandBuffer commandBuffer, int firstBinding, LongBuffer pBuffers, LongBuffer pOffsets) throws ThemisException {
        vk( () -> vkCmdBindVertexBuffers( commandBuffer, firstBinding, pBuffers, pOffsets ) );
    }

    public void cmdBindIndexBuffer(VkCommandBuffer commandBuffer, long buffer, long offset, int indexType) throws ThemisException {
        vk( () -> vkCmdBindIndexBuffer( commandBuffer, buffer, offset, indexType ) );
    }

    public void cmdDrawIndexed(VkCommandBuffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance) throws ThemisException {
        vk( () -> vkCmdDrawIndexed( commandBuffer, indexCount, instanceCount, firstIndex, vertexOffset, firstInstance ) );
    }

    public void cmdDraw(VkCommandBuffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance) throws ThemisException {
        vk( () -> vkCmdDraw( commandBuffer, vertexCount, instanceCount, firstVertex, firstInstance ) );
    }

    public void cmdBindPipeline(VkCommandBuffer commandBuffer, int pipelineBindPoint, long pipeline) throws ThemisException {
        vk( () -> vkCmdBindPipeline( commandBuffer, pipelineBindPoint, pipeline ) );
    }

    public void cmdCopyBufferToImage(VkCommandBuffer commandBuffer, long srcBuffer, long dstImage, int dstImageLayout, VkBufferImageCopy.Buffer regions) throws ThemisException {
        vk( () -> vkCmdCopyBufferToImage( commandBuffer, srcBuffer, dstImage, dstImageLayout, regions ) );
    }

    public void cmdCopyImageToBuffer(VkCommandBuffer commandBuffer, long srcImage, int srcImageLayout, long dstBuffer, VkBufferImageCopy.Buffer regions) throws ThemisException {
        vk( () -> vkCmdCopyImageToBuffer( commandBuffer, srcImage, srcImageLayout, dstBuffer, regions ) );
    }

    public void cmdCopyImage(VkCommandBuffer commandBuffer, long srcImage, int srcImageLayout, long dstImage, int dstImageLayout, VkImageCopy.Buffer pRegions) throws ThemisException {
        vk( () -> vkCmdCopyImage( commandBuffer, srcImage, srcImageLayout, dstImage, dstImageLayout, pRegions ) );
    }

    public void cmdCopyBuffer(VkCommandBuffer commandBuffer, long srcBuffer, long dstBuffer, VkBufferCopy.Buffer pRegions) throws ThemisException {
        vk( () -> vkCmdCopyBuffer( commandBuffer, srcBuffer, dstBuffer, pRegions ) );
    }

    public void cmdBlitImage(VkCommandBuffer commandBuffer, long srcImage, int srcImageLayout, long dstImage, int dstImageLayout, VkImageBlit.Buffer pRegions, int filter) throws ThemisException {
        vk( () -> vkCmdBlitImage( commandBuffer, srcImage, srcImageLayout, dstImage, dstImageLayout, pRegions, filter ) );
    }

    public void cmdPipelineBarrier(VkCommandBuffer commandBuffer, int srcStageMask, int dstStageMask, VkImageMemoryBarrier.Buffer pImageMemoryBarriers) throws ThemisException {
        vk( () -> vkCmdPipelineBarrier( commandBuffer, srcStageMask, dstStageMask, 0, null, null, pImageMemoryBarriers ) );
    }

    public void cmdPushConstants(VkCommandBuffer commandBuffer, long layout, int stageFlags, int offset, ByteBuffer pValues) throws ThemisException {
        vk( () -> vkCmdPushConstants( commandBuffer, layout, stageFlags, offset, pValues ) );
    }

}
