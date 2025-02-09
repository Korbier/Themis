package org.sc.viewer.renderactivity.geometry;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.framebuffer.VkFrameBufferAttachments;
import org.sc.themis.renderer.framebuffer.VkFrameBufferDescriptor;
import org.sc.themis.renderer.material.MaterialManager;
import org.sc.themis.renderer.material.MaterialProperties;
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
import org.sc.viewer.renderactivity.RenderPass;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class GeometryRenderPass extends RenderPass {

    public static final String FB_ATTACHMENT_PRESENT = "geometry.framebuffer.attachment.present";
    public static final String FB_ATTACHMENT_DEPTH = "geometry.framebuffer.attachment.depth";

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(GeometryRenderPass.class);

    //Framed object
    private static final FrameKey<VkFrameBuffer> FK_FRAMEBUFFER = FrameKey.of(VkFrameBuffer.class);
    private static final FrameKey<VkCommand>     FK_COMMAND = FrameKey.of(VkCommand.class);
    private static final FrameKey<VkFence>       FK_FENCE = FrameKey.of(VkFence.class);

    //Renderpass
    private VkFrameBufferAttachments frameBufferAttachments;
    private VkRenderPass renderPass;

    //Material
    private ColorMaterial defaultMaterial;
    private TextureMaterial defaultMaterial2;
    private MaterialManager materialManager;

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
        setupMaterialManager();
    }

    @Override
    public void setup(Scene scene) throws ThemisException {
        this.materialManager.compile(scene.getMaterialsProperties());
    }

    @Override
    public void cleanup() throws ThemisException {
        getRenderer().waitIdle();
        this.defaultMaterial.cleanup();
        this.defaultMaterial2.cleanup();
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();
    }

    @Override
    public void render(int frame, Scene scene, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore) throws ThemisException {

        VkCommand     command     = getFrames().get(frame, FK_COMMAND);
        VkFence       fence       = getFrames().get(frame, FK_FENCE);
        VkFrameBuffer frameBuffer = getFrames().get(frame, FK_FRAMEBUFFER);

        command.begin();
        command.beginRenderPass(this.renderPass, frameBuffer);
        command.viewportAndScissor(getExtent2D());

        for (Model model : scene.getModels()) {
            if (model.isRenderable()) {

                this.materialManager.bindMaterial(command, model);

                for (Mesh mesh : model.getMeshes()) {

                    MaterialProperties materialProperties = this.materialManager.select(mesh.getProperties(), model.getMaterialProperties());

                    if (materialProperties == null) {
                        LOG.errorf("No suitable MaterialProperties Struct found for mesh {} (model {})", mesh, model.getIdentifier());
                    }

                    this.materialManager.bindMaterialVariant(command, materialProperties, frame);

                    command.bindBuffers(mesh.getVerticesBuffer(), mesh.getIndicesBuffer());

                    for (Instance instance : model.getInstances()) {
                        command.pushConstant(VK_SHADER_STAGE_VERTEX_BIT, 0, instance.matrix());
                        command.drawIndexed(mesh.getIndiceCount());
                    }

                }
            }
        }

        command.endRenderPass();
        command.end();
        command.submit(fence, waitSemaphore, signalSemaphore);

        fence.waitForAndReset();

    }

    @Override
    public void resize() throws ThemisException {

        getFrames().remove(FK_FRAMEBUFFER);
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();

        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();

    }

    public VkFrameBufferAttachments getFramebufferAttachments() {
        return this.frameBufferAttachments;
    }

    private void setupFramebufferAttachments() throws ThemisException {
        this.frameBufferAttachments = new VkFrameBufferAttachments(getConfiguration(), getDevice(), getExtent2D());
        this.frameBufferAttachments.setup();
        this.frameBufferAttachments.depth(FB_ATTACHMENT_DEPTH, VK_FORMAT_D32_SFLOAT, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, 1);
        this.frameBufferAttachments.raw(FB_ATTACHMENT_PRESENT, getRenderer().getImageFormat());
    }

    private void setupRenderPass() throws ThemisException {
        VkRenderPassDescriptor descriptor = createSubPassDescriptor(getDevice());
        this.renderPass = new VkRenderPass(getConfiguration(), getDevice(), descriptor);
        this.renderPass.setup();
    }

    private void setupFramebuffers() throws ThemisException {
        getFrames().create(FK_FRAMEBUFFER, (frame) -> {
            VkFrameBufferDescriptor descriptor = new VkFrameBufferDescriptor(
                getExtent2D(),
                this.renderPass.getHandle(),
                this.frameBufferAttachments.get(FB_ATTACHMENT_DEPTH).getView().getHandle(),
                getImageView(frame).getHandle()
           );
            return new VkFrameBuffer(getConfiguration(), getDevice(), descriptor);
        });
    }

    private void setupFence() throws ThemisException {
        getFrames().create(FK_FENCE, () -> new VkFence(getConfiguration(), getDevice(), false));
    }

    private void setupCommand() throws ThemisException {
        getFrames().create(FK_COMMAND, () -> getRenderer().createGraphicCommand(true));
    }

    private VkRenderPassDescriptor createSubPassDescriptor(VkDevice device) {

        VkRenderPassLayout layout = new VkRenderPassLayout()
                .add(0, this.frameBufferAttachments.get(FB_ATTACHMENT_DEPTH).getFormat(), VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL, VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE, VK_ATTACHMENT_LOAD_OP_CLEAR, VK_ATTACHMENT_STORE_OP_STORE)
                .add(1, VK_FORMAT_B8G8R8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK_ATTACHMENT_LOAD_OP_CLEAR, VK_ATTACHMENT_STORE_OP_STORE, VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE);

        VkSubpass subpass = new VkSubpass(device, VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.depth(0, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
        subpass.color(1, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkRenderPassDescriptor descriptor = new VkRenderPassDescriptor(layout);
        descriptor.subpass(subpass);
        descriptor.dependency(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, 0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, 0);

        return descriptor;

    }

    private void setupMaterialManager() throws ThemisException {

        this.defaultMaterial = new ColorMaterial(
                getConfiguration(), getRenderer(), this.renderPass,
                this.getViewerActivity().getSceneDescriptorset(), this.getViewerActivity().getLighDescriptorset() );
        this.defaultMaterial.setup();

        this.defaultMaterial2 = new TextureMaterial(
                getConfiguration(), getRenderer(), this.renderPass,
                this.getViewerActivity().getSceneDescriptorset(), this.getViewerActivity().getLighDescriptorset());
        this.defaultMaterial2.setup();

        this.materialManager = new MaterialManager(this.defaultMaterial);

    }


}
