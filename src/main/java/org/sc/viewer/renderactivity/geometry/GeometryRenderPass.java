package org.sc.viewer.renderactivity.geometry;

import org.sc.themis.renderer.base.frame.FrameKey;
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
import org.sc.themis.renderer.sync.VkSemaphore;
import org.sc.themis.scene.Instance;
import org.sc.themis.scene.Mesh;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.material.ColorMaterial;
import org.sc.themis.scene.material.Material;
import org.sc.themis.scene.material.MaterialAllocator;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.renderactivity.RenderPass;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class GeometryRenderPass extends RenderPass {

    private final static String FB_ATTACHMENT_COLOR = "framebuffer.attachment.color";
    private final static String FB_ATTACHMENT_DEPTH = "framebuffer.attachment.depth";

    /*** Framed object ***/
    private final static FrameKey<VkFrameBuffer> FK_FRAMEBUFFER = FrameKey.of( VkFrameBuffer.class );
    private final static FrameKey<VkCommand>     FK_COMMAND = FrameKey.of( VkCommand.class );
    private final static FrameKey<VkFence>       FK_FENCE = FrameKey.of( VkFence.class );

    /*** Renderpass **/
    private VkFrameBufferAttachments frameBufferAttachments;
    private VkRenderPass renderPass;

    /** Materials **/
    private MaterialAllocator materialAllocator;

    public GeometryRenderPass(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void setup() throws ThemisException {
        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();
        setupCommand();
        setupFence();
        setupMaterialAllocator();
    }

    @Override
    public void setup(Scene scene) throws ThemisException {
        scene.allocateMaterial( this.materialAllocator );
    }

    @Override
    public void cleanup() throws ThemisException {
        getRenderer().waitIdle();
        this.materialAllocator.cleanup();
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();
    }

    @Override
    public void render( int frame, Scene scene, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore ) throws ThemisException {

        VkCommand     command     = getFrames().get( frame, FK_COMMAND );
        VkFence       fence       = getFrames().get( frame, FK_FENCE );
        VkFrameBuffer frameBuffer = getFrames().get( frame, FK_FRAMEBUFFER );

        command.begin();
        command.beginRenderPass( this.renderPass, frameBuffer );
        command.viewportAndScissor( getExtent2D() );

        /*** bind pipeline, descriptorset, etc **/
        for (Material material : this.materialAllocator.materials() ) {

            command.bindPipeline( material.getPipeline() );

            for ( Model model : scene.getModels() ) {
                if (model.isRenderable()) {
                    for ( Mesh mesh : model.getMeshes() ) {

                        command.bindDescriptorSets( new int[0], this.materialAllocator.getDescriptorSets( material, mesh.getProperties(), frame ) );
                        command.bindBuffers(mesh.getVerticesBuffer(), mesh.getIndicesBuffer());

                        for (Instance instance : model.getInstances() ) {
                            command.pushConstant( VK_SHADER_STAGE_VERTEX_BIT, 0, instance.matrix() );
                            command.drawIndexed(mesh.getIndiceCount());
                        }

                    }
                }
            }

        }

        command.endRenderPass();
        command.end();
        command.submit( fence, waitSemaphore, signalSemaphore );

        fence.waitForAndReset();

    }

    @Override
    public void resize() throws ThemisException {

        getFrames().remove( FK_FRAMEBUFFER );
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();

        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();

    }


    private void setupFramebufferAttachments() throws ThemisException {
        this.frameBufferAttachments = new VkFrameBufferAttachments( getConfiguration(), getDevice(), getExtent2D() );
        this.frameBufferAttachments.setup();
        this.frameBufferAttachments.raw( FB_ATTACHMENT_COLOR, getImageFormat() );
        this.frameBufferAttachments.depth( FB_ATTACHMENT_DEPTH, VK_FORMAT_D32_SFLOAT );
    }

    private void setupRenderPass() throws ThemisException {
        VkRenderPassDescriptor descriptor = createSubPassDescriptor( getDevice() );
        this.renderPass = new VkRenderPass(getConfiguration(), getDevice(), descriptor);
        this.renderPass.setup();
    }

    private void setupFramebuffers() throws ThemisException {
        getFrames().create( FK_FRAMEBUFFER, ( frame ) -> {
            VkFrameBufferDescriptor descriptor = new VkFrameBufferDescriptor(
                getExtent2D(),
                this.renderPass.getHandle(),
                getImageView( frame ).getHandle(),
                this.frameBufferAttachments.get( FB_ATTACHMENT_DEPTH ).getView().getHandle()
            );
            return new VkFrameBuffer( getConfiguration(), getDevice(), descriptor );
        });
    }

    private void setupFence() throws ThemisException {
        getFrames().create( FK_FENCE, () -> new VkFence( getConfiguration(), getDevice(), false ) );
    }

    private void setupCommand() throws ThemisException {
        getFrames().create( FK_COMMAND, () -> getRenderer().createGraphicCommand( true ) );
    }

    private VkRenderPassDescriptor createSubPassDescriptor(VkDevice device) {

        VkRenderPassLayout layout = new VkRenderPassLayout()
                .add( 0, VK_FORMAT_B8G8R8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK_ATTACHMENT_LOAD_OP_CLEAR, VK_ATTACHMENT_STORE_OP_STORE, VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE )
                .add( 1, this.frameBufferAttachments.get( FB_ATTACHMENT_DEPTH ).getFormat(), VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE, VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE);

        VkSubpass subpass = new VkSubpass( device, VK_PIPELINE_BIND_POINT_GRAPHICS );
        subpass.color( 0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL );
        subpass.depth( 1, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL );

        VkRenderPassDescriptor descriptor = new VkRenderPassDescriptor( layout );
        descriptor.subpass( subpass );
        descriptor.dependency( 0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, 0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, 0 );

        return descriptor;

    }

    private void setupMaterialAllocator() throws ThemisException {

        this.materialAllocator = new MaterialAllocator();

        Material color = new ColorMaterial( getConfiguration(), getRenderer(), this.renderPass, getViewerActivity().getSceneDescriptorset() );
        color.setup();

        this.materialAllocator.add( color );

    }
}
