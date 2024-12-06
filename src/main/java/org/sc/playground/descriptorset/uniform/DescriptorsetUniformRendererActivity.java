package org.sc.playground.descriptorset.uniform;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.playground.shared.BaseRendererActivity;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.pipeline.*;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.vulkan.VK10.*;

public class DescriptorsetUniformRendererActivity extends BaseRendererActivity {

    private final static String SHADER_VERTEX_SOURCE = "src/main/resources/playground/descriptorset/uniform/vertex_shader.glsl";
    private final static String SHADER_VERTEX_COMPILED = "target/playground/descriptorset/uniform/vertex_shader.spirv";
    private final static String SHADER_FRAGMENT_SOURCE = "src/main/resources/playground/descriptorset/uniform/fragment_shader.glsl";
    private final static String SHADER_FRAGMENT_COMPILED = "target/playground/descriptorset/uniform/fragment_shader.spirv";

    /*** Framed object ***/
    private final static FrameKey<VkBuffer>        FK_UNIFORM_BUFFER = FrameKey.of( VkBuffer.class );
    private final static FrameKey<VkBuffer>        FK_DYNAMIC_BUFFER = FrameKey.of( VkBuffer.class );
    private final static FrameKey<VkDescriptorSet> FK_DESCRIPTORSET  = FrameKey.of( VkDescriptorSet.class );

    private VkShaderProgram shaderProgram;
    private VkPipelineLayout pipelineLayout;
    private VkPipeline pipeline;

    private VkDescriptorSetLayout descriptorLayout;
    private VkDescriptorPool descriptorPool;

    private Frames frames;

    public DescriptorsetUniformRendererActivity(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void render(Scene scene, long tpf) throws ThemisException {

        this.renderer.acquire();

        int frame = this.renderer.getCurrentFrame();

        VkCommand       command     = getCommand( frame );
        VkFence         fence       = getFence( frame );
        VkFrameBuffer   framebuffer = getFramebuffer( frame );
        VkBuffer        uniformBuffer = this.frames.get( frame, FK_UNIFORM_BUFFER );
        VkBuffer        dynamicBuffer = this.frames.get( frame, FK_DYNAMIC_BUFFER );
        VkDescriptorSet descriptorSet = this.frames.get( frame, FK_DESCRIPTORSET );

        uniformBuffer.set( 0, 1.0f, 0.0f, 0.0f, 1.0f );
        dynamicBuffer.set( dynamicBuffer.getAlignedOffset(0), 0.0f, 1.0f, 0.0f, 1.0f );
        dynamicBuffer.set( dynamicBuffer.getAlignedOffset(1), 0.0f, 0.0f, 1.0f, 1.0f );

        command.begin();
        command.beginRenderPass( this.renderPass, framebuffer );
        command.viewportAndScissor( this.renderer.getExtent() );
        command.bindPipeline( this.pipeline );
        command.bindDescriptorSets( new int[] {dynamicBuffer.getAlignedOffset( 0 )} , descriptorSet );
        command.draw( 3, 1, 0, 0);
        command.bindDescriptorSets( new int[] {dynamicBuffer.getAlignedOffset( 1 )} , descriptorSet );
        command.draw( 3, 1, 3, 0);
        command.endRenderPass();
        command.end();
        command.submit( fence, this.renderer.getAcquireSemanphore( frame ), this.renderer.getPresentSemaphore( frame ) );

        fence.waitFor();
        fence.reset();

    }

    public void setupPipeline() throws ThemisException {
        this.frames = new Frames( this.renderer.getFrameCount() );
        this.setupDescriptorSets();
        this.setupShaderProgram();
        this.setupPipelineAndLayout();
    }

    private void setupDescriptorSets() throws ThemisException {

        this.descriptorLayout = new VkDescriptorSetLayout(
            getConfiguration(), this.renderer.getDevice(),
            VkDescriptorSetBinding.uniform( 0, VK_SHADER_STAGE_FRAGMENT_BIT ),
            VkDescriptorSetBinding.dynamicUniform( 1, VK_SHADER_STAGE_FRAGMENT_BIT )

        );
        this.descriptorLayout.setup();

        this.descriptorPool = new VkDescriptorPool( getConfiguration(), this.renderer.getDevice(), this.renderer.getFrameCount(), this.descriptorLayout );
        this.descriptorPool.setup();

        this.frames.create( FK_UNIFORM_BUFFER, () -> {
            VkBuffer buffer = new VkBuffer( getConfiguration(), this.renderer.getDevice(), this.renderer.getMemoryAllocator(), VkBufferDescriptor.descriptorsetUniform( MemorySizeUtils.VEC4F ));
            buffer.setup();
            return buffer;
        });

        this.frames.create( FK_DYNAMIC_BUFFER, () -> {
            VkBuffer buffer = new VkBuffer( getConfiguration(), this.renderer.getDevice(), this.renderer.getMemoryAllocator(), VkBufferDescriptor.descriptorsetDynamicUniform( MemorySizeUtils.VEC4F, 2 ));
            buffer.setup();
            return buffer;
        });

        this.frames.create( FK_DESCRIPTORSET, ( frame ) -> {
            VkDescriptorSet descriptorSet = new VkDescriptorSet(getConfiguration(), this.renderer.getDevice(), this.descriptorPool, this.descriptorLayout );
            descriptorSet.setup();
            descriptorSet.bind( 0, this.frames.get( frame, FK_UNIFORM_BUFFER ) );
            descriptorSet.bind( 1, this.frames.get( frame, FK_DYNAMIC_BUFFER ) );
            return descriptorSet;
        });

    }

    @Override
    public void cleanupPipeline() throws ThemisException {
        this.frames.cleanup();
        this.descriptorPool.cleanup();
        this.descriptorLayout.cleanup();
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

        this.pipelineLayout = new VkPipelineLayout(getConfiguration(), this.renderer.getDevice(), new VkPushConstantRange[0], this.descriptorLayout );
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
