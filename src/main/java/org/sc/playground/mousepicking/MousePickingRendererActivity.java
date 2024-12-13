package org.sc.playground.mousepicking;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.framebuffer.VkFrameBufferAttachments;
import org.sc.themis.renderer.framebuffer.VkFrameBufferDescriptor;
import org.sc.themis.renderer.pipeline.*;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.renderer.renderpass.VkRenderPassDescriptor;
import org.sc.themis.renderer.renderpass.VkRenderPassLayout;
import org.sc.themis.renderer.renderpass.VkSubpass;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.scene.Instance;
import org.sc.themis.scene.Mesh;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

/**
 * 1 pass, 3 subpass :
 *  subpass 0 : draw instance identier
 *  subpass 1 : pickup fragment at mouse position
 *  subpass 2 : draw instance with selected instance
 *
 */
public class MousePickingRendererActivity extends RendererActivity {

    private final static String FB_ATTACHMENT_PRESENTATION = "framebuffer.attachment.presentation";
    private final static String FB_ATTACHMENT_DEPTH        = "framebuffer.attachment.depth";
    private final static String FB_ATTACHMENT_IDENTIFIER   = "framebuffer.attachment.identifier";

    private final static String SHADER_0_VERTEX_SOURCE = "src/main/resources/playground/mousepicking/0_vertex_shader.glsl";
    private final static String SHADER_0_VERTEX_COMPILED = "target/playground/mousepicking/0_vertex_shader.spirv";
    private final static String SHADER_0_FRAGMENT_SOURCE = "src/main/resources/playground/mousepicking/0_fragment_shader.glsl";
    private final static String SHADER_0_FRAGMENT_COMPILED = "target/playground/mousepicking/0_fragment_shader.spirv";


    /*** Framed object ***/
    private final static FrameKey<VkFrameBuffer> FK_FRAMEBUFFER = FrameKey.of( VkFrameBuffer.class );
    private final static FrameKey<VkCommand>     FK_COMMAND = FrameKey.of( VkCommand.class );
    private final static FrameKey<VkFence>       FK_FENCE = FrameKey.of( VkFence.class );

    protected Renderer renderer;
    protected VkFrameBufferAttachments frameBufferAttachments;
    protected VkRenderPass renderPass;

    /** subpass 0 **/
    private VkShaderProgram shaderProgram0;
    private VkPipelineLayout pipelineLayout0;
    private VkPipeline pipeline0;

    public MousePickingRendererActivity(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void setup(Renderer renderer) throws ThemisException {
        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();
        setupShaderProgram0();
        setupPipeline0();
//TODO        setupShaderProgram1();
//TODO        setupPipeline1();
//TODO        setupShaderProgram2();
//TODO        setupPipeline2();
        setupCommand();
        setupFence();
    }

    private void setupShaderProgram0() throws ThemisException {
        try {
            VkShaderSourceCompiler.compileShaderIfChanged(SHADER_0_VERTEX_SOURCE, SHADER_0_VERTEX_COMPILED, Shaderc.shaderc_glsl_vertex_shader);
            VkShaderSourceCompiler.compileShaderIfChanged(SHADER_0_FRAGMENT_SOURCE, SHADER_0_FRAGMENT_COMPILED, Shaderc.shaderc_glsl_fragment_shader);
            VkShaderProgramStage vertexStage = new VkShaderProgramStage(VK_SHADER_STAGE_VERTEX_BIT, Files.readAllBytes(Paths.get(SHADER_0_VERTEX_COMPILED)));
            VkShaderProgramStage fragmentStage = new VkShaderProgramStage(VK_SHADER_STAGE_FRAGMENT_BIT, Files.readAllBytes(Paths.get(SHADER_0_FRAGMENT_COMPILED)));
            this.shaderProgram0 = new VkShaderProgram(getConfiguration(), renderer.getDevice(), vertexStage, fragmentStage);
            this.shaderProgram0.setup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void render(Scene scene, long tpf) throws ThemisException {


        int frame = this.renderer.acquire();

        VkCommand       command     = this.renderer.getFrames().get( frame, FK_COMMAND );
        VkFence         fence       = this.renderer.getFrames().get( frame, FK_FENCE );
        VkFrameBuffer   framebuffer = this.renderer.getFrames().get( frame, FK_FRAMEBUFFER );

        command.begin();
        command.beginRenderPass( this.renderPass, framebuffer );
        command.viewportAndScissor( this.renderer.getExtent() );
        command.bindPipeline(this.pipeline0);

        for ( Model model : scene.getModels() ) {
            if ( model.isRenderable() ) {
                for (Mesh mesh : model.getMeshes() ) {
                    command.bindBuffers(mesh.getVerticesBuffer(), mesh.getIndicesBuffer());
                    for (Instance instance : model.getInstances() ) {
                        command.pushConstant( VK_SHADER_STAGE_VERTEX_BIT, 0, instance.matrix() );
                        command.drawIndexed(mesh.getIndiceCount());
                    }

                }
            }
        }

        command.endRenderPass();
        command.end();
        command.submit( fence, this.renderer.getAcquireSemanphore( frame ), this.renderer.getPresentSemaphore( frame ) );

        fence.waitForAndReset();

    }

    @Override
    public void resize() throws ThemisException {

        this.renderer.getFrames().remove( FK_FRAMEBUFFER );
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();

        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();

    }

    @Override
    public void cleanup() throws ThemisException {

        this.pipeline0.cleanup();
        this.pipelineLayout0.cleanup();
        this.shaderProgram0.cleanup();

        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();

    }

    private void setupFramebuffers() throws ThemisException {
        this.renderer.getFrames().create( FK_FRAMEBUFFER, ( frame ) -> {
            VkFrameBufferDescriptor descriptor = new VkFrameBufferDescriptor( this.renderer.getExtent(), this.renderPass.getHandle(), this.renderer.getImageView( frame ).getHandle() );
            return new VkFrameBuffer( getConfiguration(), this.renderer.getDevice(), descriptor );
        });
    }

    private void setupFramebufferAttachments() throws ThemisException {
        this.frameBufferAttachments = new VkFrameBufferAttachments( getConfiguration(), renderer.getDevice(), this.renderer.getExtent() );
        this.frameBufferAttachments.setup();
        this.frameBufferAttachments.depth( FB_ATTACHMENT_DEPTH, VK_FORMAT_D32_SFLOAT );
        this.frameBufferAttachments.raw(   FB_ATTACHMENT_PRESENTATION, renderer.getImageFormat() );
        this.frameBufferAttachments.color( FB_ATTACHMENT_IDENTIFIER, VK_FORMAT_R16G16B16A16_UNORM, VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT, VK_SAMPLE_COUNT_1_BIT );
    }

    private void setupRenderPass() throws ThemisException {
        VkRenderPassDescriptor descriptor = createSubPassDescriptor( renderer.getDevice() );
        this.renderPass = new VkRenderPass(getConfiguration(), renderer.getDevice(), descriptor);
        this.renderPass.setup();
    }

    private void setupFence() throws ThemisException {
        this.renderer.getFrames().create( FK_FENCE, () -> new VkFence( getConfiguration(), this.renderer.getDevice(), false ) );
    }

    private void setupCommand() throws ThemisException {
        this.renderer.getFrames().create( FK_COMMAND, () -> this.renderer.createGraphicCommand( true ) );
    }

    private VkRenderPassDescriptor createSubPassDescriptor(VkDevice device) {

        VkRenderPassLayout layout = new VkRenderPassLayout()
                .add(0, this.renderer.getImageFormat(), VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK_ATTACHMENT_LOAD_OP_CLEAR, VK_ATTACHMENT_STORE_OP_STORE, VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .add(1, this.frameBufferAttachments.get( FB_ATTACHMENT_DEPTH ).getFormat(), VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE, VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .add(2, this.frameBufferAttachments.get( FB_ATTACHMENT_IDENTIFIER ).getFormat(), VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_ATTACHMENT_LOAD_OP_CLEAR, VK_ATTACHMENT_STORE_OP_STORE, VK_ATTACHMENT_LOAD_OP_DONT_CARE, VK_ATTACHMENT_STORE_OP_DONT_CARE );

        VkSubpass subpass1 = new VkSubpass( device, VK_PIPELINE_BIND_POINT_GRAPHICS );
        subpass1.depth( 1, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL );
        subpass1.color( 2, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL );

        VkSubpass subpass2 = new VkSubpass( device, VK_PIPELINE_BIND_POINT_GRAPHICS );
        subpass2.color( 2, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL );

        VkSubpass subpass3 = new VkSubpass( device, VK_PIPELINE_BIND_POINT_GRAPHICS );
        subpass3.color( 0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL );

        VkRenderPassDescriptor descriptor = new VkRenderPassDescriptor( layout );
        descriptor.subpass( subpass1, subpass2, subpass3 );
        descriptor.dependency(VK_SUBPASS_EXTERNAL, 0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, 0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, 0);
        descriptor.dependency(0, 1, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VK_ACCESS_INPUT_ATTACHMENT_READ_BIT, VK_DEPENDENCY_BY_REGION_BIT);
        descriptor.dependency(1, 2, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VK_ACCESS_INPUT_ATTACHMENT_READ_BIT, VK_DEPENDENCY_BY_REGION_BIT);

        return descriptor;

    }

    private void setupPipeline0() throws ThemisException {

        this.pipelineLayout0 = new VkPipelineLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            new VkPushConstantRange[0]
        );
        this.pipelineLayout0.setup();

        try (MemoryStack stack = MemoryStack.stackPush() ) {

            VkVertexInputStateDescriptor descriptor1 = new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX)
                    .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Position
                    .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Normal
                    .attribute( VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F ) //Texture
                    .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Tangent
                    .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ); //Bitangent

            VkVertexInputState inputState = new VkVertexInputState(descriptor1);
            inputState.setup( stack );

            this.pipeline0 = new VkPipeline(
                    getConfiguration(),
                    this.renderer.getDevice(),
                    new VkPipelineDescriptor(this.renderPass, 0, false, 1, false, 1, 1, 1),
                    this.shaderProgram0,
                    this.pipelineLayout0,
                    inputState
            );

            this.pipeline0.setup();

        }
    }

}
