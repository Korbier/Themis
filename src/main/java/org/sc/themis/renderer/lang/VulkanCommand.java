package org.sc.themis.renderer.lang;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdBlitImage;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBufferToImage;
import static org.lwjgl.vulkan.VK10.vkCmdCopyImage;
import static org.lwjgl.vulkan.VK10.vkCmdCopyImageToBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdNextSubpass;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkResetCommandBuffer;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.lang.exception.VkDeviceLostException;
import org.sc.themis.renderer.lang.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.lang.exception.VkOutOfHostMemoryException;
import org.sc.themis.renderer.lang.exception.VulkanException;
import org.sc.themis.shared.exception.ThemisException;

public class VulkanCommand extends Vulkan {

  /**
   * Create a new command pool object
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCreateCommandPool.html">Official spec.</a>
   *
   * @param device the logical device that creates the command pool.
   * @param pCreateInfo a pointer to a VkCommandPoolCreateInfo structure specifying the state of the command pool object
   * @param pCommandPool a pointer to a VkCommandPool handle in which the created pool is returned.
   * @throws VulkanException On error, an exception occured
   */
  public void createCommandPool(VkDevice device, VkCommandPoolCreateInfo pCreateInfo, LongBuffer pCommandPool) throws VulkanException {
    vk(
      () -> vkCreateCommandPool(device, pCreateInfo, null, pCommandPool),
      (errno) -> {
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY) throw new VkOutOfHostMemoryException();
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY) throw new VkOutOfDeviceMemoryException();
      }
    );
  }

  /**
   * Destroy a command pool object
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkDestroyCommandPool.html">Official spec.</a>
   *
   * @param device the logical device that destroys the command pool.
   * @param commandPool the handle of the command pool to destroy.
   * @throws VulkanException On error, an exception occured
   */
  public void destroyCommandPool(VkDevice device, long commandPool) throws VulkanException {
    vk(() -> vkDestroyCommandPool(device, commandPool, null));
  }

  /**
   * Allocate command buffers from an existing command pool
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkAllocateCommandBuffers.html">Official spec.</a>
   *
   * @param device the logical device that owns the command pool.
   * @param pAllocateInfo a pointer to a VkCommandBufferAllocateInfo structure describing parameters of the allocation.
   * @param pCommandBuffers a pointer to an array of VkCommandBuffer handles in which the resulting command buffer objects are returned.
   * @throws VulkanException On error, an exception occured
   */
  public void allocateCommandBuffers(VkDevice device, VkCommandBufferAllocateInfo pAllocateInfo, PointerBuffer pCommandBuffers) throws ThemisException {
    vk(
      () -> vkAllocateCommandBuffers(device, pAllocateInfo, pCommandBuffers),
      (errno) -> {
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY) throw new VkOutOfHostMemoryException();
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY) throw new VkOutOfDeviceMemoryException();
      }
    );
  }

  /**
   * Free command buffers
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkFreeCommandBuffers.html">Official spec.</a>
   *
   * @param device the logical device that owns the command pool.
   * @param commandPool the command pool from which the command buffers were allocated.
   * @param pCommandBuffers a pointer to an array of handles of command buffers to free.
   * @throws VulkanException On error, an exception occured
   */
  public void freeCommandBuffers(VkDevice device, long commandPool, VkCommandBuffer pCommandBuffers) throws VulkanException {
    vk(() -> vkFreeCommandBuffers(device, commandPool, pCommandBuffers));
  }

  /**
   * Start recording a command buffer
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkBeginCommandBuffer.html">Official spec.</a>
   *
   * @param commandBuffer the handle of the command buffer which is to be put in the recording state.
   * @param pBeginInfo a pointer to a VkCommandBufferBeginInfo structure defining additional information about how the command buffer begins recording.
   * @throws VulkanException On error, an exception occured
   */
  public void beginCommandBuffer(VkCommandBuffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo) throws VulkanException {
    vk(
      () -> vkBeginCommandBuffer(commandBuffer, pBeginInfo),
      (errno) -> {
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY) throw new VkOutOfHostMemoryException();
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY) throw new VkOutOfDeviceMemoryException();
      }
    );
  }

  /**
   * Start recording a command buffer
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/endCommandBuffer.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer to complete recording.
   * @throws VulkanException On error, an exception occured
   */
  public void endCommandBuffer(VkCommandBuffer commandBuffer) throws VulkanException {
    vk(
      () -> vkEndCommandBuffer(commandBuffer),
      (errno) -> {
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY) throw new VkOutOfHostMemoryException();
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY) throw new VkOutOfDeviceMemoryException();
      }
    );
  }

  /**
   * Reset a command buffer to the initial state
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkResetCommandBuffer.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer to reset.
   * @throws VulkanException On error, an exception occured
   */
  public void resetCommandBuffer(VkCommandBuffer commandBuffer) throws VulkanException {
    vk(() -> vkResetCommandBuffer(commandBuffer, VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT));
  }

  /**
   * Submits a sequence of semaphores or command buffers to a queue
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkQueueSubmit.html">Official spec.</a>
   *
   * @param queue the queue that the command buffers will be submitted to.
   * @param pSubmits a pointer to an array of VkSubmitInfo structures, each specifying a command buffer submission batch.
   * @param fence an optional handle to a fence to be signaled once all submitted command buffers have completed execution.
   * @throws VulkanException On error, an exception occured
   */
  public void queueSubmit(VkQueue queue, VkSubmitInfo.Buffer pSubmits, long fence) throws VulkanException {
    vk(
      () -> vkQueueSubmit(queue, pSubmits, fence),
      (errno) -> {
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY) throw new VkOutOfHostMemoryException();
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY) throw new VkOutOfDeviceMemoryException();
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_DEVICE_LOST) throw new VkDeviceLostException();
      });
  }

  /**
   * Submits a sequence of semaphores or command buffers to a queue.
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkQueueSubmit.html">Official spec.</a>
   *
   * @param queue the queue that the command buffers will be submitted to.
   * @param pSubmit a pointer to a VkSubmitInfo structures, specifying a command buffer submission batch.
   * @param fence an optional handle to a fence to be signaled once all submitted command buffers have completed execution.
   * @throws VulkanException On error, an exception occured
   */
  public void queueSubmit(VkQueue queue, VkSubmitInfo pSubmit, long fence) throws VulkanException {
    vk(
      () -> vkQueueSubmit(queue, pSubmit, fence),
      (errno) -> {
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY) throw new VkOutOfHostMemoryException();
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY) throw new VkOutOfDeviceMemoryException();
        if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_DEVICE_LOST) throw new VkDeviceLostException();
      }
    );
  }

  /**
   * Begin a new render pass.
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdBeginRenderPass.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer in which to record the command.
   * @param pRenderPassBegin a pointer to a VkRenderPassBeginInfo structure specifying the render pass to begin an instance of, and the framebuffer the instance uses.
   * @param contents a VkSubpassContents value specifying how the commands in the first subpass will be provided.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdBeginRenderPass(VkCommandBuffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, int contents) throws VulkanException {
    vk(() -> vkCmdBeginRenderPass(commandBuffer, pRenderPassBegin, contents));
  }

  /**
   * End the current render pass.
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdEndRenderPass.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer in which to end the current render pass instance.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdEndRenderPass(VkCommandBuffer commandBuffer) throws VulkanException {
    vk(() -> vkCmdEndRenderPass(commandBuffer));
  }

  /**
   * Set the viewport dynamically for a command buffer.
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdSetViewport.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command will be recorded.
   * @param viewports a pointer to an array of VkViewport structures specifying viewport parameters.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdSetViewport(VkCommandBuffer commandBuffer, VkViewport.Buffer viewports) throws VulkanException {
    vk(() -> vkCmdSetViewport(commandBuffer, 0, viewports));
  }

  /**
   * Set scissor rectangles dynamically for a command buffer
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdSetScissor.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command will be recorded.
   * @param scissors a pointer to an array of VkRect2D structures defining scissor rectangles.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdSetScissor(VkCommandBuffer commandBuffer, VkRect2D.Buffer scissors) throws VulkanException {
    vk(() -> vkCmdSetScissor(commandBuffer, 0, scissors));
  }

  /**
   * Transition to the next subpass of a render pass.
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdNextSubpass.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer in which to end the current render pass instance.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdNextSubpass(VkCommandBuffer commandBuffer, int contents) throws VulkanException {
    vk(() -> vkCmdNextSubpass(commandBuffer, contents));
  }

  /**
   * Bind vertex buffers to a command buffer.
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdBindVertexBuffers.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command is recorded.
   * @param firstBinding the index of the first vertex input binding whose state is updated by the command.
   * @param pBuffers a pointer to an array of buffer handles.
   * @param pOffsets a pointer to an array of buffer offsets.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdBindVertexBuffers(VkCommandBuffer commandBuffer, int firstBinding, LongBuffer pBuffers, LongBuffer pOffsets) throws VulkanException {
    vk(() -> vkCmdBindVertexBuffers(commandBuffer, firstBinding, pBuffers, pOffsets));
  }

  /**
   * Bind vertex buffers to a command buffer.
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdBindIndexBuffer.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command is recorded.
   * @param buffer the buffer being bound.
   * @param offset the starting offset in bytes within buffer used in index buffer address calculations.
   * @param indexType a VkIndexType value specifying the size of the indices.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdBindIndexBuffer(VkCommandBuffer commandBuffer, long buffer, long offset, int indexType) throws VulkanException {
    vk(() -> vkCmdBindIndexBuffer(commandBuffer, buffer, offset, indexType));
  }

  /**
   * Draw primitives with indexed vertices
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdDrawIndexed.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command is recorded.
   * @param indexCount the number of vertices to draw.
   * @param instanceCount the number of instances to draw.
   * @param firstIndex the base index within the index buffer.
   * @param vertexOffset the value added to the vertex index before indexing into the vertex buffer.
   * @param firstInstance the instance ID of the first instance to draw.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdDrawIndexed( VkCommandBuffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance)
      throws VulkanException {
    vk(() -> vkCmdDrawIndexed(commandBuffer, indexCount, instanceCount, firstIndex, vertexOffset, firstInstance));
  }

  /**
   * Draw primitives
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdDraw.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command is recorded.
   * @param vertexCount the number of vertices to draw.
   * @param instanceCount the number of instances to draw.
   * @param firstVertex  the index of the first vertex to draw.
   * @param firstInstance the instance ID of the first instance to draw.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdDraw(
      VkCommandBuffer commandBuffer,
      int vertexCount, int instanceCount, int firstVertex, int firstInstance) throws VulkanException {
    vk(() -> vkCmdDraw(commandBuffer, vertexCount, instanceCount, firstVertex, firstInstance));
  }

  /**
   * Bind a pipeline object to a command buffer.
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdBindPipeline.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer that the pipeline will be bound to.
   * @param pipelineBindPoint a VkPipelineBindPoint value specifying to which bind point the pipeline is bound.
   * @param pipeline the pipeline to be bound.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdBindPipeline(VkCommandBuffer commandBuffer, int pipelineBindPoint, long pipeline) throws VulkanException {
    vk(() -> vkCmdBindPipeline(commandBuffer, pipelineBindPoint, pipeline));
  }

  /**
   * Copy data from a buffer into an image.
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdCopyBufferToImage.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command will be recorded.
   * @param srcBuffer the source buffer.
   * @param dstImage the destination image.
   * @param  dstImageLayout the layout of the destination image subresources for the copy.
   * @param  regions a pointer to an array of VkBufferImageCopy structures specifying the regions to copy.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdCopyBufferToImage(VkCommandBuffer commandBuffer, long srcBuffer, long dstImage, int dstImageLayout, VkBufferImageCopy.Buffer regions)
      throws VulkanException {
    vk(() -> vkCmdCopyBufferToImage(commandBuffer, srcBuffer, dstImage, dstImageLayout, regions));
  }

  /**
   * Copy image data into a buffer.
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdCopyImageToBuffer.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command will be recorded.
   * @param srcImage the source image.
   * @param srcImageLayout the layout of the source image subresources for the copy.
   * @param dstBuffer the destination buffer.
   * @param regions a pointer to an array of VkBufferImageCopy structures specifying the regions to copy.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdCopyImageToBuffer(VkCommandBuffer commandBuffer, long srcImage, int srcImageLayout, long dstBuffer, VkBufferImageCopy.Buffer regions)
      throws VulkanException {
    vk(() -> vkCmdCopyImageToBuffer(commandBuffer, srcImage, srcImageLayout, dstBuffer, regions));
  }

  /**
   * Copy data between images
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdCopyImage.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command will be recorded.
   * @param srcImage the source image.
   * @param srcImageLayout the current layout of the source image subresource.
   * @param dstImage the destination image.
   * @param dstImageLayout the current layout of the destination image subresource.
   * @param pRegions a pointer to an array of VkImageCopy structures specifying the regions to copy.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdCopyImage(VkCommandBuffer commandBuffer, long srcImage, int srcImageLayout, long dstImage, int dstImageLayout, VkImageCopy.Buffer pRegions)
      throws VulkanException {
    vk(() -> vkCmdCopyImage(commandBuffer, srcImage, srcImageLayout, dstImage, dstImageLayout, pRegions));
  }

  /**
   * Copy data between buffer regions
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdCopyBuffer.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command will be recorded.
   * @param srcBuffer the source buffer.
   * @param dstBuffer the destination buffer.
   * @param pRegions a pointer to an array of VkBufferCopy structures specifying the regions to copy.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdCopyBuffer(VkCommandBuffer commandBuffer, long srcBuffer, long dstBuffer, VkBufferCopy.Buffer pRegions) throws VulkanException {
    vk(() -> vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, pRegions));
  }

  /**
   * Copy regions of an image, potentially performing format conversion
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/cmdBlitImage.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command will be recorded.
   * @param srcImage the source image.
   * @param srcImageLayout the layout of the source image subresources for the blit.
   * @param dstImage the destination image.
   * @param dstImageLayout the layout of the destination image subresources for the blit.
   * @param pRegions a pointer to an array of VkImageBlit structures specifying the regions to blit.
   * @param filter a VkFilter specifying the filter to apply if the blits require scaling.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdBlitImage(
      VkCommandBuffer commandBuffer,
      long srcImage, int srcImageLayout,
      long dstImage, int dstImageLayout,
      VkImageBlit.Buffer pRegions, int filter)
      throws VulkanException {
    vk(() -> vkCmdBlitImage(commandBuffer, srcImage, srcImageLayout, dstImage, dstImageLayout, pRegions, filter));
  }

  /**
   * Insert a memory dependency
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdPipelineBarrier.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer into which the command is recorded.
   * @param srcStageMask a bitmask of VkPipelineStageFlagBits specifying the source stages.
   * @param dstStageMask a bitmask of VkPipelineStageFlagBits specifying the destination stages.
   * @param pImageMemoryBarriers a pointer to an array of VkImageMemoryBarrier structures.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdPipelineBarrier(VkCommandBuffer commandBuffer, int srcStageMask, int dstStageMask, VkImageMemoryBarrier.Buffer pImageMemoryBarriers)
      throws VulkanException {
    vk(() -> vkCmdPipelineBarrier(commandBuffer, srcStageMask, dstStageMask, 0, null, null, pImageMemoryBarriers));
  }

  /**
   * Update the values of push constants
   * @see <a href="https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdPushConstants.html">Official spec.</a>
   *
   * @param commandBuffer the command buffer in which the push constant update will be recorded.
   * @param layout the pipeline layout used to program the push constant updates.
   * @param stageFlags a bitmask of VkShaderStageFlagBits specifying the shader stages that will use the push constants in the updated range.
   * @param offset the start offset of the push constant range to update, in units of bytes.
   * @param pValues a pointer to an array of size bytes containing the new push constant values.
   * @throws VulkanException On error, an exception occured
   */
  public void cmdPushConstants(VkCommandBuffer commandBuffer, long layout, int stageFlags, int offset, ByteBuffer pValues) throws VulkanException {
    vk(() -> vkCmdPushConstants(commandBuffer, layout, stageFlags, offset, pValues));
  }
}
