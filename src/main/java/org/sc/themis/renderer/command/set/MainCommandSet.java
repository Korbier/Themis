package org.sc.themis.renderer.command.set;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.sc.themis.renderer.command.VkCommandBuffer;
import org.sc.themis.renderer.command.VkCommandInheritanceInfo;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.renderer.sync.VkSemaphore;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class MainCommandSet extends VkCommandSet {

    public MainCommandSet(Configuration configuration, VkCommandBuffer buffer) {
        super(configuration, buffer);
    }

    public void reset() throws ThemisException {
        vkCommand().resetCommandBuffer(buffer().getHandle());
    }

    public void begin(int flags, VkCommandInheritanceInfo inheritanceInfo) throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            VkCommandBufferBeginInfo commandBufferBeginInfo = createCommandBufferBeginInfo(stack, flags, inheritanceInfo);
            vkCommand().beginCommandBuffer( buffer().getHandle(), commandBufferBeginInfo );
        }
    }

    public void end() throws ThemisException {
        vkCommand().endCommandBuffer( buffer().getHandle() );
    }

    public void submit(VkFence fence, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore, IntBuffer dstStageMasks) throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            VkSubmitInfo submitInfo = createSubmitInfo( stack, waitSemaphore, signalSemaphore, dstStageMasks );
            vkCommand().queueSubmit( buffer().getQueue().getHandle(), submitInfo, fence != null ? fence.getHandle() : VK_NULL_HANDLE );
        }
    }

    private VkCommandBufferBeginInfo createCommandBufferBeginInfo(MemoryStack stack, int flags, VkCommandInheritanceInfo inheritanceInfo) {

        VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .flags(flags);

        if ( !buffer().isPrimary() ) {

            if (inheritanceInfo == null) {
                throw new RuntimeException("Secondary buffers must declare inheritance info");
            }

            VkCommandBufferInheritanceInfo vkInheritanceInfo = createCommandBufferInheritanceInfo( stack, inheritanceInfo); ;
            cmdBufInfo.pInheritanceInfo(vkInheritanceInfo);

        }

        return cmdBufInfo;

    }

    private VkCommandBufferInheritanceInfo createCommandBufferInheritanceInfo(MemoryStack stack, VkCommandInheritanceInfo inheritanceInfo) {
        return VkCommandBufferInheritanceInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
                .renderPass(inheritanceInfo.vkRenderPass())
                .subpass(inheritanceInfo.subPass())
                .framebuffer(inheritanceInfo.vkFrameBuffer());
    }

    private VkSubmitInfo createSubmitInfo(MemoryStack stack, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore, IntBuffer dstStageMasks) {

        VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pCommandBuffers(stack.pointers( buffer().getHandle()));

        if ( signalSemaphore != null ) {
            submitInfo.pSignalSemaphores(stack.longs(signalSemaphore.getHandle()));
        } else {
            submitInfo.pSignalSemaphores(null);
        }

        if ( waitSemaphore != null ) {
            LongBuffer lbWaitSemaphore = stack.longs(waitSemaphore.getHandle());
            submitInfo.waitSemaphoreCount(lbWaitSemaphore.capacity())
                    .pWaitSemaphores(lbWaitSemaphore)
                    .pWaitDstStageMask(dstStageMasks);
        } else {
            submitInfo.waitSemaphoreCount( 0 );
        }

        return submitInfo;

    }


}
