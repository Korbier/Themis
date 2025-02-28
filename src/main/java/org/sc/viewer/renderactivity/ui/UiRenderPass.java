package org.sc.viewer.renderactivity.ui;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

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
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.renderer.sync.VkSemaphore;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.pen.Pencil;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;
import org.sc.viewer.renderactivity.RenderPass;
import org.sc.themis.scene.pen.DrawCommand;
import org.sc.themis.scene.pen.DrawVertex;

/**
 * UI Renderpass.
 **/
public class UiRenderPass extends RenderPass {

    private static final String FB_ATTACHMENT_COLOR = "ui.framebuffer.attachment.color";

    // Framed object
    private static final FrameKey<VkFrameBuffer> FK_FRAMEBUFFER = FrameKey.of(VkFrameBuffer.class);
    private static final FrameKey<VkCommand>     FK_COMMAND = FrameKey.of(VkCommand.class);

    // Renderpass
    private VkFrameBufferAttachments frameBufferAttachments;
    private VkRenderPass renderPass;

    private BackPipeline backPipeline;
    private FrontPipeline frontPipeline;

    private VkBuffer drawCommandVertexBuffer;
    private VkBuffer drawCommandIndiceBuffer;

    public UiRenderPass(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void setup() throws ThemisException {
        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();
        setupCommand();
        setupBackPipeline();
        setupFrontPipeline();
    }

    private void setupBackPipeline() throws ThemisException {
        this.backPipeline = new BackPipeline(getConfiguration(), getDevice(), getViewerActivity(), this.renderPass);
        this.backPipeline.setup();
    }

    private void setupFrontPipeline() throws ThemisException {
        this.frontPipeline = new FrontPipeline(getConfiguration(), getDevice(), getViewerActivity(), this.renderPass);
        this.frontPipeline.setup();
    }

    @Override
    public void setup(Scene scene) throws ThemisException {
        this.frontPipeline.updateAll(scene);
    }

    @Override
    public void cleanup() throws ThemisException {
        this.drawCommandVertexBuffer.cleanup();
        this.drawCommandIndiceBuffer.cleanup();
        this.backPipeline.cleanup();
        this.frontPipeline.cleanup();
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

        command.bindPipeline(this.backPipeline.getPipeline());
        command.bindDescriptorSets(new int[0], getViewerActivity().getGeometryDescriptorset().getDescriptorSet(frame));
        command.draw(3, 1, 0, 0);

        this.updatePencilBuffers(scene.getPencil());

        command.bindPipeline(this.frontPipeline.getPipeline());
        command.bindDescriptorSets(new int[0], this.frontPipeline.getDescriptorset(frame));
        command.bindBuffers(this.drawCommandVertexBuffer, this.drawCommandIndiceBuffer);
        command.drawIndexed(scene.getPencil().getIndiceSize());

        command.endRenderPass();
        command.end();

        command.submit(fence, waitSemaphore, signalSemaphore);

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
            .add(0, VK_FORMAT_B8G8R8A8_SRGB,
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
                    VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_STORE,
                    VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE);

        VkSubpass subpass = new VkSubpass(device, VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.color(0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

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
                    getImageView(frame).getHandle()
            );
            return new VkFrameBuffer(getConfiguration(), getDevice(), descriptor);
        });
    }

    private void setupCommand() throws ThemisException {
        getFrames().create(FK_COMMAND, () -> getRenderer().createGraphicCommand(true));
    }

    private void updatePencilBuffers(Pencil pencil) throws ThemisException {

        long dataSize = (long) pencil.getDataSize() * MemorySizeUtils.FLOAT;
        long indiceSize = (long) pencil.getIndiceSize() * MemorySizeUtils.INT;

        if (this.drawCommandVertexBuffer == null || this.drawCommandVertexBuffer.getRequestedSize() < dataSize) {

            if (this.drawCommandVertexBuffer != null) {
                this.drawCommandVertexBuffer.cleanup();
            }

            VkBufferDescriptor decriptor = VkBufferDescriptor.vertexBuffer(dataSize);
            this.drawCommandVertexBuffer = new VkBuffer(
                    getConfiguration(), this.getDevice(),
                    getViewerActivity().getRenderer().getMemoryAllocator(),
                    decriptor);
            this.drawCommandVertexBuffer.setup();

        }

        this.drawCommandVertexBuffer.set(0, pencil.getData());

        if (this.drawCommandIndiceBuffer == null || this.drawCommandIndiceBuffer.getRequestedSize() < indiceSize) {

            if (this.drawCommandIndiceBuffer != null) {
                this.drawCommandIndiceBuffer.cleanup();
            }

            VkBufferDescriptor decriptorIndices = VkBufferDescriptor.indiceBuffer(indiceSize);
            this.drawCommandIndiceBuffer = new VkBuffer(
                    getConfiguration(), this.getDevice(),
                    getViewerActivity().getRenderer().getMemoryAllocator(),
                    decriptorIndices);
            this.drawCommandIndiceBuffer.setup();

        }

        this.drawCommandIndiceBuffer.set(0, pencil.getIndices());

    }

}
