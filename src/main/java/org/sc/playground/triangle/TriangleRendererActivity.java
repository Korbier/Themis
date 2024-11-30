package org.sc.playground.triangle;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.framebuffer.VkFrameBufferAttachments;
import org.sc.themis.renderer.framebuffer.VkFrameBufferDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.FramedObject;

public class TriangleRendererActivity extends RendererActivity {

    private final String FB_ATTACHMENT_COLOR = "framebuffer.attachment.color";

    private Renderer renderer;

    private VkFrameBufferAttachments frameBufferAttachments;
    private FramedObject<VkFrameBuffer> framebuffers;

    public TriangleRendererActivity(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void setup( Renderer renderer ) throws ThemisException {
        this.renderer = renderer;
        setupFramebufferAttachments();
        setupFramebuffers();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.framebuffers.accept( VkFrameBuffer::cleanup );
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
                        -1l, //Todo put renderpass handle here
                        this.renderer.getImageView( frame ).getHandle()
                    );
                    VkFrameBuffer framebuffer = new VkFrameBuffer( getConfiguration(), this.renderer.getDevice(), descriptor );
                    framebuffer.setup();
                    return framebuffer;
                }
        );
    }



}
