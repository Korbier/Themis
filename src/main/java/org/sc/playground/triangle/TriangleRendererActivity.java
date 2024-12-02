package org.sc.playground.triangle;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.framebuffer.VkFrameBufferAttachments;
import org.sc.themis.renderer.framebuffer.VkFrameBufferDescriptor;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.renderer.renderpass.VkRenderPassDescriptor;
import org.sc.themis.renderer.renderpass.VkRenderPassLayout;
import org.sc.themis.renderer.renderpass.VkSubpass;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.FramedObject;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class TriangleRendererActivity extends RendererActivity {

    private final String FB_ATTACHMENT_COLOR = "framebuffer.attachment.color";

    private Renderer renderer;

    private VkFrameBufferAttachments frameBufferAttachments;
    private VkRenderPass renderPass;
    private FramedObject<VkFrameBuffer> framebuffers;

    public TriangleRendererActivity(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void setup( Renderer renderer ) throws ThemisException {
        this.renderer = renderer;
        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();

    }

    private void setupRenderPass() throws ThemisException {
        VkRenderPassDescriptor descriptor = createSubPassDescriptor( renderer.getDevice() );
        this.renderPass = new VkRenderPass(getConfiguration(), renderer.getDevice(), descriptor);
        this.renderPass.setup();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.framebuffers.accept( VkFrameBuffer::cleanup );
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();
    }

    private void setupFramebufferAttachments() throws ThemisException {
        this.frameBufferAttachments = new VkFrameBufferAttachments( getConfiguration(), renderer.getDevice(), this.renderer.getExtent() );
        this.frameBufferAttachments.setup();
        this.frameBufferAttachments.raw( FB_ATTACHMENT_COLOR, renderer.getImageFormat() );
    }

    private void setupFramebuffers() throws ThemisException {
        this.framebuffers = FramedObject.of(
                this.renderer.getFrameCount(),
                ( frame ) -> {
                    VkFrameBufferDescriptor descriptor = new VkFrameBufferDescriptor(
                        this.renderer.getExtent(),
                        this.renderPass.getHandle(),
                        this.renderer.getImageView( frame ).getHandle()
                    );
                    VkFrameBuffer framebuffer = new VkFrameBuffer( getConfiguration(), this.renderer.getDevice(), descriptor );
                    framebuffer.setup();
                    return framebuffer;
                }
        );
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
