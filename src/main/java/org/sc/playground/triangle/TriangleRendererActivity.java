package org.sc.playground.triangle;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.activity.RendererActivity;
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
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.FramedObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class TriangleRendererActivity extends RendererActivity {

    private final static String SHADER_VERTEX_SOURCE = "src/main/resources/playground/triangle/vertex_shader.glsl";
    private final static String SHADER_VERTEX_COMPILED = "target/playground/triangle/vertex_shader.spirv";
    private final static String SHADER_FRAGMENT_SOURCE = "src/main/resources/playground/triangle/fragment_shader.glsl";
    private final static String SHADER_FRAGMENT_COMPILED = "target/playground/triangle/fragment_shader.spirv";
    private final static String FB_ATTACHMENT_COLOR = "framebuffer.attachment.color";

    private Renderer renderer;

    private VkFrameBufferAttachments frameBufferAttachments;
    private VkRenderPass renderPass;
    private FramedObject<VkFrameBuffer> framebuffers;

    private VkShaderProgram shaderProgram;
    private VkPipelineLayout pipelineLayout;
    private VkPipeline pipeline;

    private FramedObject<VkCommand> commands;
    private FramedObject<VkFence> fences;

    public TriangleRendererActivity(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void setup( Renderer renderer ) throws ThemisException {
        this.renderer = renderer;
        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();
        setupShaderProgram();
        setupPipeline();
        setupCommand();
        setupFence();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.renderer.waitIdle();
        this.fences.accept( VkFence::cleanup );
        this.commands.accept( VkCommand::cleanup );
        this.pipeline.cleanup();
        this.pipelineLayout.cleanup();
        this.shaderProgram.cleanup();
        this.framebuffers.accept( VkFrameBuffer::cleanup );
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();
    }

    @Override
    public void render(Scene scene, long tpf) throws ThemisException {

        this.renderer.acquire();

        int frame = this.renderer.getCurrentFrame();
        VkCommand     command = this.commands.get( frame );
        VkFence       fence = this.fences.get( frame );
        VkFrameBuffer framebuffer = this.framebuffers.get( frame );

        command.begin();
        command.beginRenderPass( this.renderPass, framebuffer );
        command.viewportAndScissor( this.renderer.getExtent() );
        command.bindPipeline( this.pipeline );
        command.draw( 3, 1, 0, 0);
        command.endRenderPass();
        command.end();
        command.submit( fence, this.renderer.getAcquireSemanphore( frame ), this.renderer.getPresentSemaphore( frame ) );

        fence.waitFor();
        fence.reset();

    }

    @Override
    public void resize() throws ThemisException {

        this.framebuffers.accept( VkFrameBuffer::cleanup );
        this.renderPass.cleanup();
        this.frameBufferAttachments.cleanup();

        setupFramebufferAttachments();
        setupRenderPass();
        setupFramebuffers();

    }

    private void setupFence() throws ThemisException {
        this.fences = FramedObject.of( this.renderer.getFrameCount(), () -> {
            VkFence fence = new VkFence(getConfiguration(), this.renderer.getDevice(), false);
            fence.setup();
            return fence;
        });
    }

    private void setupCommand() throws ThemisException {
        this.commands = FramedObject.of( this.renderer.getFrameCount(), () -> this.renderer.createGraphicCommand( true ) );
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

    private void setupShaderProgram() throws ThemisException {

        try {

            VkShaderSourceCompiler.compileShaderIfChanged(SHADER_VERTEX_SOURCE, SHADER_VERTEX_COMPILED, Shaderc.shaderc_glsl_vertex_shader);
            VkShaderSourceCompiler.compileShaderIfChanged(SHADER_FRAGMENT_SOURCE, SHADER_FRAGMENT_COMPILED, Shaderc.shaderc_glsl_fragment_shader);

            VkShaderProgramStage vertexStage = new VkShaderProgramStage(VK_SHADER_STAGE_VERTEX_BIT, Files.readAllBytes(Paths.get(SHADER_VERTEX_COMPILED)));
            VkShaderProgramStage fragmentStage = new VkShaderProgramStage(VK_SHADER_STAGE_FRAGMENT_BIT, Files.readAllBytes(Paths.get(SHADER_FRAGMENT_COMPILED)));

            this.shaderProgram = new VkShaderProgram(getConfiguration(), renderer.getDevice(), vertexStage, fragmentStage);
            this.shaderProgram.setup();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void setupPipeline() throws ThemisException {

        this.pipelineLayout = new VkPipelineLayout(getConfiguration(), this.renderer.getDevice(), new VkPushConstantRange[0] );
        this.pipelineLayout.setup();

        try (MemoryStack stack = MemoryStack.stackPush() ) {

            VkVertexInputState inputState = new VkVertexInputState();
            inputState.setup( stack );

            this.pipeline = new VkPipeline(
                getConfiguration(),
                this.renderer.getDevice(),
                new VkPipelineDescriptor(this.renderPass, 0, false, 1, false, 1, 1, 1),
                this.shaderProgram,
                this.pipelineLayout,
                inputState
            );

            this.pipeline.setup();

        }

    }


}
