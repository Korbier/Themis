package org.sc.themis.renderer.command.set;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkViewport;
import org.sc.themis.renderer.command.VkCommandBuffer;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.renderer.renderpass.VkRenderPassAttachment;
import org.sc.themis.renderer.renderpass.VkRenderPassLayout;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class RenderPassCommandSet extends VkCommandSet {

    public RenderPassCommandSet(Configuration configuration, VkCommandBuffer buffer) {
        super(configuration, buffer);
    }

    public void begin(VkRenderPass renderPass, VkFrameBuffer frameBuffer, boolean executeSecondaryCommandBuffer ) throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            VkClearValue.Buffer clearValues = createClearValues( stack, renderPass );
            VkRenderPassBeginInfo renderPassBeginInfo = createRenderPassBeginInfo(stack, clearValues, renderPass, frameBuffer );
            int contents = executeSecondaryCommandBuffer ? VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS : VK_SUBPASS_CONTENTS_INLINE;
            vkCommand().cmdBeginRenderPass( buffer().getHandle(), renderPassBeginInfo, contents);
        };
    }

    public void endRenderPass() throws ThemisException {
        vkCommand().cmdEndRenderPass( buffer().getHandle() );
    }

    public void nextSubPass() throws ThemisException {
        vkCommand().cmdNextSubpass( buffer().getHandle(), VK_SUBPASS_CONTENTS_INLINE );
    }

    public void scissor( int left, int top, int width, int height  ) throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack)
                    .extent(it -> it.width(width).height(height))
                    .offset(it -> it.x(left).y(top));
            vkCommand().cmdSetScissor(buffer().getHandle(), scissor);
        }
    }

    public void viewport( int width, int height ) throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            VkViewport.Buffer viewports = VkViewport.calloc(1, stack)
                    .x(0).y(height)
                    .height(-height).width(width)
                    .minDepth(0.0f).maxDepth(1.0f);
            vkCommand().cmdSetViewport(buffer().getHandle(), viewports);
        }
    }

    public void bindBuffers(VkBuffer vertices, VkBuffer indices) throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            LongBuffer offsets = stack.mallocLong(1).put(0, 0L);
            LongBuffer vertexBuffer = stack.mallocLong(1).put(0, vertices.getHandle());
            vkCommand().cmdBindVertexBuffers(buffer().getHandle(), 0, vertexBuffer, offsets);
            vkCommand().cmdBindIndexBuffer(buffer().getHandle(), indices.getHandle(), 0, VK_INDEX_TYPE_UINT32);
        }
    }

    public void bindBuffer( int binding, VkBuffer vertices ) throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            LongBuffer offsets = stack.mallocLong(1).put(0, 0L);
            LongBuffer vertexBuffer = stack.mallocLong(1).put(0, vertices.getHandle());
            vkCommand().cmdBindVertexBuffers(buffer().getHandle(), binding, vertexBuffer, offsets);
        }
    }

    public void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance ) throws ThemisException {
        vkCommand().cmdDraw( buffer().getHandle(), vertexCount, instanceCount, firstVertex, firstInstance );
    }

    public void drawIndexed( int indexCount,  int instanceCount, int firstIndex, int vertexOffset, int firstInstance ) throws ThemisException {
        vkCommand().cmdDrawIndexed( buffer().getHandle(), indexCount, instanceCount, firstIndex, vertexOffset, firstInstance );
    }

    private VkClearValue.Buffer createClearValues( MemoryStack stack, VkRenderPass renderPass ) {

        VkRenderPassLayout renderPassLayout = renderPass.getDescriptor().getLayout();

        VkClearValue.Buffer clearValues = VkClearValue.calloc(renderPassLayout.size(), stack);

        for (Integer attachmentIdx : renderPassLayout.keys() ) {
            VkRenderPassAttachment attachment = renderPassLayout.get( attachmentIdx );
            if ( attachment.finalLayout() == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL ) {
                clearValues.apply( v -> v.depthStencil().depth( 1.0f ) );
            } else {
                clearValues.apply(v -> v.color().float32(0, 0.0f).float32(1, 0.0f).float32(2, 0.0f).float32(3, 1.0f));
            }
        }

        return clearValues.flip();

    }

    private VkRenderPassBeginInfo createRenderPassBeginInfo(MemoryStack stack, VkClearValue.Buffer clearValues, VkRenderPass renderPass, VkFrameBuffer frameBuffer) {
        return VkRenderPassBeginInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                .renderPass( renderPass.getHandle() )
                .pClearValues( clearValues )
                .renderArea(a -> a.extent().set(frameBuffer.getDescriptor().width(), frameBuffer.getDescriptor().height()))
                .framebuffer(frameBuffer.getHandle());
    }
}
