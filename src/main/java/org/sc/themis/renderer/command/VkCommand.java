package org.sc.themis.renderer.command;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.sc.themis.renderer.command.set.*;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.pipeline.VkPipeline;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.image.VkImage;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.renderer.sync.VkSemaphore;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.function.Consumer;

import static org.lwjgl.vulkan.VK10.*;

public class VkCommand extends VkCommandSet {

    private final static Logger logger = LoggerFactory.getLogger( VkCommand.class );

    private final MainCommandSet main;
    private final RenderPassCommandSet renderpass;
    private final PipelineCommandSet pipeline;
    private final ResourceSet resource;

    public VkCommand(Configuration configuration, VkCommandBuffer buffer ) {
        super( configuration, buffer );
        this.main = new MainCommandSet( configuration, buffer );
        this.renderpass = new RenderPassCommandSet( configuration, buffer );
        this.pipeline = new PipelineCommandSet( configuration, buffer );
        this.resource = new ResourceSet( configuration, buffer );
    }

    private MainCommandSet mainSet() {
        return this.main;
    }

    private RenderPassCommandSet renderPass() {
        return this.renderpass;
    }

    private PipelineCommandSet pipeline() {
        return this.pipeline;
    }

    private ResourceSet resource() {
        return this.resource;
    }

    public void reset() throws ThemisException {
        mainSet().reset();
    }

    public void begin() throws ThemisException {
        mainSet().begin(0, null);
    }

    public void begin(int flags, VkCommandInheritanceInfo inheritanceInfo) throws ThemisException {
        mainSet().begin( flags, inheritanceInfo );
    }

    public void end() throws ThemisException {
        mainSet().end();
    }

    public void submit(VkFence fence, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore, IntBuffer dstStageMasks) throws ThemisException {
        mainSet().submit( fence, waitSemaphore, signalSemaphore, dstStageMasks );
    }

    public void submit(VkFence fence, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore ) throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            mainSet().submit( fence, waitSemaphore, signalSemaphore, stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT) );
        }
    }

    public void submit( VkFence fence ) throws ThemisException {
        mainSet().submit( fence, null, null, null );
    }

    /**** Render pass ****/
    public void beginRenderPass(VkRenderPass renderPass, VkFrameBuffer framebuffer ) throws ThemisException {
        renderPass().begin( renderPass, framebuffer, false );
    }

    public void beginRenderPass(VkRenderPass renderPass, VkFrameBuffer framebuffer, boolean executeSecondary ) throws ThemisException {
        renderPass().begin( renderPass, framebuffer, executeSecondary );
    }

    public void endRenderPass() throws ThemisException {
        renderPass().endRenderPass();
    }

    public void viewportAndScissor(VkExtent2D extent) throws ThemisException {
        viewportAndScissor( extent.width(), extent.height() );
    }

    public void viewportAndScissor(int width, int height) throws ThemisException {
        renderPass().viewport( width, height );
        renderPass().scissor( 0, 0, width, height  );
    }

    public void bindBuffers(VkBuffer vertices, VkBuffer indices) throws ThemisException {
        renderPass().bindBuffers( vertices, indices );
    }

    public void bindBuffer(int binding, VkBuffer vertices) throws ThemisException {
        renderPass().bindBuffer( binding, vertices );
    }

    public void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance) throws ThemisException {
        renderPass().draw( vertexCount, instanceCount, firstVertex, firstInstance );
    }

    public void drawIndexed( int indicesCount ) throws ThemisException {
        renderPass().drawIndexed( indicesCount, 1, 0, 0, 0 );
    }

    public void drawIndexed( int indexCount,  int instanceCount, int firstIndex, int vertexOffset, int firstInstance ) throws ThemisException {
        renderPass().drawIndexed( indexCount, instanceCount, firstIndex, vertexOffset, firstInstance );
    }

    /**** Pipeline ****/
    public void bindPipeline( VkPipeline pipeline ) throws ThemisException {
        bindPipeline( pipeline, false );
    }

    public void bindPipeline( VkPipeline pipeline, boolean compute ) throws ThemisException {
        pipeline().bindPipeline( pipeline, compute ? VK_PIPELINE_BIND_POINT_COMPUTE : VK_PIPELINE_BIND_POINT_GRAPHICS );
    }

    /**** Buffer ****/
    public void copy( VkBuffer srcBuffer, VkBuffer dstBuffer ) throws ThemisException {
        resource().copy( srcBuffer, dstBuffer, ResourceSet.Region.of( 0, 0, srcBuffer.getRequestedSize() ) );
    }

    public void copy( VkBuffer srcBuffer, VkBuffer dstBuffer, ResourceSet.Region... regions ) throws ThemisException {
        resource().copy( srcBuffer, dstBuffer, regions );
    }

    public void copy( VkBuffer srcBuffer, VkImage dstImage ) throws ThemisException {
        resource().copy( srcBuffer, dstImage );
    }


    /**** Image ****/
    public void layout(VkImage image, int srcLayout, int dstLayout, int srcPipelineStage, int dstPipelineStage, int srcAccessMask, int dstAccessMask, Consumer<VkImageSubresourceRange> subResourceRange ) throws ThemisException {
        resource().layout( image, srcLayout, dstLayout, srcPipelineStage, dstPipelineStage, srcAccessMask, dstAccessMask, subResourceRange );
    }

    public void generateMipMaps( VkImage image, int mipLevel) throws ThemisException {
        resource().generateMipMaps( image, mipLevel );
    }

}
