package org.sc.playground.pushconstant;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.playground.shared.BaseRendererActivity;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.pipeline.*;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public class PushConstantRendererActivity extends BaseRendererActivity {

    private final static String SHADER_VERTEX_SOURCE = "src/main/resources/playground/pushconstant/vertex_shader.glsl";
    private final static String SHADER_VERTEX_COMPILED = "target/playground/pushconstant/vertex_shader.spirv";
    private final static String SHADER_FRAGMENT_SOURCE = "src/main/resources/playground/pushconstant/fragment_shader.glsl";
    private final static String SHADER_FRAGMENT_COMPILED = "target/playground/pushconstant/fragment_shader.spirv";

    private VkShaderProgram shaderProgram;
    private VkPipelineLayout pipelineLayout;
    private VkPipeline pipeline;

    public PushConstantRendererActivity(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void render(Scene scene, long tpf) throws ThemisException {

        int frame = this.renderer.acquire();

        VkCommand     command     = getCommand( frame );
        VkFence       fence       = getFence( frame );
        VkFrameBuffer framebuffer = getFramebuffer( frame );

        command.begin();
        command.beginRenderPass( this.renderPass, framebuffer );
        command.viewportAndScissor( this.renderer.getExtent() );
        command.bindPipeline( this.pipeline );
        command.pushConstant( VK_SHADER_STAGE_VERTEX_BIT, 0, 0.2f, 0.2f, 0.0f );
        command.pushConstant( VK_SHADER_STAGE_FRAGMENT_BIT, MemorySizeUtils.MAT4x4F,  0.0f, 1.0f, 0.0f );
        command.draw( 3, 1, 0, 0);
        command.endRenderPass();
        command.end();
        command.submit( fence, this.renderer.getAcquireSemaphore( frame ), this.renderer.getPresentSemaphore( frame ) );

        fence.waitForAndReset();

    }

    @Override
    public void setupPipeline() throws ThemisException {
        this.setupShaderProgram();
        this.setupPipelineAndLayout();
    }

    @Override
    public void cleanupPipeline() throws ThemisException {
        this.pipeline.cleanup();
        this.pipelineLayout.cleanup();
        this.shaderProgram.cleanup();
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

    private void setupPipelineAndLayout() throws ThemisException {

        this.pipelineLayout = new VkPipelineLayout(
            getConfiguration(), this.renderer.getDevice(),
            new VkPushConstantRange[] {
                new VkPushConstantRange( VK_SHADER_STAGE_VERTEX_BIT, 0, MemorySizeUtils.MAT4x4F ),
                new VkPushConstantRange( VK_SHADER_STAGE_FRAGMENT_BIT, MemorySizeUtils.MAT4x4F, MemorySizeUtils.MAT4x4F )
            }
        );
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
