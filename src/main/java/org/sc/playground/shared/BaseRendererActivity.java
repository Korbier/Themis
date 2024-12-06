package org.sc.playground.shared;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.framebuffer.VkFrameBufferAttachments;
import org.sc.themis.renderer.framebuffer.VkFrameBufferDescriptor;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.renderer.renderpass.VkRenderPassDescriptor;
import org.sc.themis.renderer.renderpass.VkRenderPassLayout;
import org.sc.themis.renderer.renderpass.VkSubpass;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public abstract class BaseRendererActivity extends RendererActivity {

    private final static String FB_ATTACHMENT_COLOR = "framebuffer.attachment.color";

    /*** Framed object ***/
    private final static FrameKey<VkFrameBuffer> FK_FRAMEBUFFER = FrameKey.of( VkFrameBuffer.class );
    private final static FrameKey<VkCommand>     FK_COMMAND = FrameKey.of( VkCommand.class );
    private final static FrameKey<VkFence>       FK_FENCE = FrameKey.of( VkFence.class );

    protected Renderer renderer;
    protected VkFrameBufferAttachments frameBufferAttachments;
    protected VkRenderPass renderPass;

    protected Frames frames;

    public BaseRendererActivity(Configuration configuration) {
        super(configuration);
    }

    public abstract void setupPipeline() throws ThemisException;
    public abstract void cleanupPipeline() throws ThemisException ;

    @Override
    public void setup( Renderer renderer ) throws ThemisException {
        this.renderer = renderer;
        this.frames = new Frames( this.renderer.getFrameCount() );
        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();
        setupCommand();
        setupFence();
        setupPipeline();
    }

    @Override
    public void cleanup() throws ThemisException {
        cleanupPipeline();
        this.frames.cleanup();
        this.renderer.waitIdle();
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();
    }

    @Override
    public void resize() throws ThemisException {

        this.frames.remove( FK_FRAMEBUFFER, true );
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();

        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();

    }

    protected VkFrameBuffer getFramebuffer( int frame ) {
        return this.frames.get( frame, FK_FRAMEBUFFER );
    }

    protected VkCommand getCommand( int frame ) {
        return this.frames.get( frame, FK_COMMAND );
    }

    protected VkFence getFence( int frame ) {
        return this.frames.get( frame, FK_FENCE );
    }

    private void setupFence() throws ThemisException {
        this.frames.create( FK_FENCE, () -> {
            VkFence fence = new VkFence(getConfiguration(), this.renderer.getDevice(), false);
            fence.setup();
            return fence;
        } );
    }

    private void setupCommand() throws ThemisException {
        this.frames.create( FK_COMMAND, () -> this.renderer.createGraphicCommand( true ) );
    }

    private void setupRenderPass() throws ThemisException {
        VkRenderPassDescriptor descriptor = createSubPassDescriptor( renderer.getDevice() );
        this.renderPass = new VkRenderPass(getConfiguration(), renderer.getDevice(), descriptor);
        this.renderPass.setup();
    }

    private void setupFramebufferAttachments() throws ThemisException {
        this.frameBufferAttachments = new VkFrameBufferAttachments( getConfiguration(), renderer.getDevice(), this.renderer.getExtent() );
        this.frameBufferAttachments.setup();
        this.frameBufferAttachments.raw( FB_ATTACHMENT_COLOR, renderer.getImageFormat() );
    }

    private void setupFramebuffers() throws ThemisException {
        this.frames.create( FK_FRAMEBUFFER, ( frame ) -> {
            VkFrameBufferDescriptor descriptor = new VkFrameBufferDescriptor(
                    this.renderer.getExtent(),
                    this.renderPass.getHandle(),
                    this.renderer.getImageView( frame ).getHandle()
            );
            VkFrameBuffer framebuffer = new VkFrameBuffer( getConfiguration(), this.renderer.getDevice(), descriptor );
            framebuffer.setup();
            return framebuffer;
        });
    }

    private VkRenderPassDescriptor createSubPassDescriptor(VkDevice device) {

        VkRenderPassLayout layout = new VkRenderPassLayout()
                .add( 0, VK_FORMAT_B8G8R8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK_ATTACHMENT_LOAD_OP_CLEAR, VK_ATTACHMENT_STORE_OP_STORE, VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE );

        VkSubpass subpass = new VkSubpass( device, VK_PIPELINE_BIND_POINT_GRAPHICS );
        subpass.color( 0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL );

        VkRenderPassDescriptor descriptor = new VkRenderPassDescriptor( layout );
        descriptor.subpass( subpass );
        descriptor.dependency( 0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, 0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, 0 );

        return descriptor;

    }

}
