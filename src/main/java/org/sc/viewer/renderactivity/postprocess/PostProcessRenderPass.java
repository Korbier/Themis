package org.sc.viewer.renderactivity.postprocess;

import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_LOAD;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

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
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.ViewerContext;
import org.sc.viewer.renderactivity.RenderPass;
import org.sc.viewer.renderactivity.ViewerRendererActivity;

/**
 * Post processing render pass.
 */
public class PostProcessRenderPass extends RenderPass {

    private static final String FB_ATTACHMENT_COLOR = "postprocess.framebuffer.attachment.color";

    private final ViewerContext context;

    // Framed object
    private static final FrameKey<VkFrameBuffer> FK_FRAMEBUFFER = FrameKey.of(VkFrameBuffer.class);
    private static final FrameKey<VkCommand>     FK_COMMAND = FrameKey.of(VkCommand.class);

    // Renderpass
    private VkFrameBufferAttachments frameBufferAttachments;
    private VkRenderPass renderPass;

    private PostProcessors postProcessors;

    /**
     * Default constructor.
     *
     * @param configuration Configuration
     * @param context Context
     */
    public PostProcessRenderPass(Configuration configuration, ViewerContext context) {
        super(configuration);
        this.context = context;
    }

    @Override
    public void setup() throws ThemisException {
        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();
        setupCommand();
        setupPostProcessors();
    }

    @Override
    public void setup(Scene scene) throws ThemisException {
    }

    private void setupPostProcessors() throws ThemisException {
        this.postProcessors = new PostProcessors(
                getConfiguration(), getRenderer(),
                this.renderPass,
                getViewerActivity().getSceneDescriptorset(),
                getViewerActivity().getGeometryDescriptorset()
        );
        this.postProcessors.setup();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.postProcessors.cleanup();
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();
    }

    @Override
    public void render(int frame, Scene scene, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore, VkFence fence)
            throws ThemisException {

        VkCommand     command     = getFrames().get(frame, FK_COMMAND);
        VkFrameBuffer frameBuffer = getFrames().get(frame, FK_FRAMEBUFFER);

        command.begin();
        command.beginRenderPass(this.renderPass, frameBuffer);
        command.viewportAndScissor(getExtent2D());

        for (String postprocessor : this.postProcessors.get(PostProcessor.Frequency.PER_VERTEX)) {
            if (this.context.isPostProcessorEnabled(postprocessor)) {
                this.postProcessors.getPipeline(postprocessor).bind(command, frame);
                this.renderPerVertex(scene, command);
            }
        }

        command.endRenderPass();
        command.end();

        command.submit(fence, waitSemaphore, signalSemaphore);

    }

    private void renderPerVertex(Scene scene, VkCommand command) throws ThemisException {
        for (Model model : scene.getModels()) {
            if (model.isRenderable()) {
                for (Mesh mesh : model.getMeshes()) {
                    command.bindBuffers(mesh.getVerticesBuffer(), mesh.getIndicesBuffer());
                    for (Instance instance : model.getInstances()) {
                        command.pushConstant(VK_SHADER_STAGE_VERTEX_BIT, 0, instance.matrix());
                        command.drawIndexed(mesh.getIndiceCount());
                    }
                }
            }
        }
    }

    @Override
    public void resize() throws ThemisException {

        getFrames().remove(FK_FRAMEBUFFER);
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();

        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();

        this.postProcessors.resize(
            this.renderPass,
            getViewerActivity().getSceneDescriptorset(),
            getViewerActivity().getGeometryDescriptorset()
        );

    }

    private void setupFramebufferAttachments() throws ThemisException {
        this.frameBufferAttachments = new VkFrameBufferAttachments(getConfiguration(), getDevice(), getExtent2D());
        this.frameBufferAttachments.setup();
        this.frameBufferAttachments.raw(FB_ATTACHMENT_COLOR, getImageFormat());
    }

    private void setupRenderPass() throws ThemisException {
        VkRenderPassDescriptor descriptor = createSubPassDescriptor(getDevice());
        this.renderPass = new VkRenderPass(getConfiguration(), getDevice(), descriptor);
        this.renderPass.setup();
    }

    private VkRenderPassDescriptor createSubPassDescriptor(VkDevice device) {

        VkRenderPassLayout layout = new VkRenderPassLayout()
            .add(0,
                getGeometryFrameBufferAttachments().get(ViewerRendererActivity.GEOMETRY_FB_ATTACHMENT_DEPTH).getFormat(),
                VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL, VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL,
                VK_ATTACHMENT_LOAD_OP_LOAD, VK_ATTACHMENT_STORE_OP_DONT_CARE,
                VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_STORE)
            .add(1,
                getGeometryFrameBufferAttachments().get(ViewerRendererActivity.GEOMETRY_FB_ATTACHMENT_COLOR).getFormat(),
                VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_STORE,
                VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE);

        VkSubpass subpass = new VkSubpass(device, VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.depth(0, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
        subpass.color(1, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkRenderPassDescriptor descriptor = new VkRenderPassDescriptor(layout);
        descriptor.subpass(subpass);
        descriptor.dependency(0,
                VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, 0);

        return descriptor;

    }

    private void setupFramebuffers() throws ThemisException {
        getFrames().create(FK_FRAMEBUFFER, (frame) -> {
            VkFrameBufferDescriptor descriptor = new VkFrameBufferDescriptor(
                getExtent2D(),
                this.renderPass.getHandle(),
                getGeometryFrameBufferAttachments().get(ViewerRendererActivity.GEOMETRY_FB_ATTACHMENT_DEPTH).getView().getHandle(),
                getGeometryFrameBufferAttachments().get(ViewerRendererActivity.GEOMETRY_FB_ATTACHMENT_COLOR).getView().getHandle()
            );
            return new VkFrameBuffer(getConfiguration(), getDevice(), descriptor);
        });
    }

    private void setupCommand() throws ThemisException {
        getFrames().create(FK_COMMAND, () -> getRenderer().createGraphicCommand(true));
    }

}
