package org.sc.viewer.renderactivity.postprocess;

import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.pipeline.*;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.scene.Instance;
import org.sc.themis.scene.Mesh;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.descriptorset.InputDescriptorSet;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;

public class PostProcessorPipeline {

    private final Configuration configuration;
    private final Renderer renderer;
    private final PostProcessor postProcessor;
    private final VkRenderPass renderpass;
    private final SceneDescriptorSet sceneDescriptorset;
    private final InputDescriptorSet geometryAttachmentDescriptorset;
    private VkShaderProgram shaderProgram;
    private VkPipelineLayout pipelineLayout;
    private VkPipeline pipeline;

    public PostProcessorPipeline(
        Configuration configuration, Renderer renderer,
        VkRenderPass renderpass,
        SceneDescriptorSet sceneDescriptorset,
        InputDescriptorSet geometryAttachmentDescriptorset,
        PostProcessor postProcessor ) {
        this.configuration = configuration;
        this.renderer = renderer;
        this.postProcessor = postProcessor;
        this.renderpass = renderpass;
        this.sceneDescriptorset = sceneDescriptorset;
        this.geometryAttachmentDescriptorset = geometryAttachmentDescriptorset;
    }

    public void setup() throws ThemisException {
        setupShaderProgram();
        setupPipelineLayout();
        setupPipeline();
    }

    public void cleanup() throws ThemisException {
        this.pipeline.cleanup();
        this.pipelineLayout.cleanup();
        this.shaderProgram.cleanup();
    }

    public void render(Scene scene, VkCommand command, int frame ) throws ThemisException {

        command.bindPipeline( this.pipeline );

        command.bindDescriptorSets(
            new int[0],
            this.sceneDescriptorset.getDescriptorSet( frame ),
            this.geometryAttachmentDescriptorset.getDescriptorSet( frame )
        );

        for ( Model model : scene.getModels() ) {
            if (model.isRenderable()) {
                for (Mesh mesh : model.getMeshes()) {
                    command.bindBuffers(mesh.getVerticesBuffer(), mesh.getIndicesBuffer());
                    for (Instance instance : model.getInstances() ) {
                        command.pushConstant( VK_SHADER_STAGE_VERTEX_BIT, 0, instance.matrix() );
                        command.drawIndexed(mesh.getIndiceCount());
                    }
                }
            }
        }


    }

    private void setupShaderProgram() throws ThemisException {

        this.shaderProgram = new VkShaderProgram(
            this.configuration,
            this.renderer.getDevice(),
            new VkShaderProgramStage(VK_SHADER_STAGE_VERTEX_BIT, this.postProcessor.getVertexShader()),
            new VkShaderProgramStage(VK_SHADER_STAGE_GEOMETRY_BIT, this.postProcessor.getGeometryShader()),
            new VkShaderProgramStage(VK_SHADER_STAGE_FRAGMENT_BIT, this.postProcessor.getFragmentShader())
        );

        this.shaderProgram.setup();

    }

    private void setupPipelineLayout() throws ThemisException {
        this.pipelineLayout = new VkPipelineLayout(
            this.configuration,
            this.renderer.getDevice(),
            new VkPushConstantRange[] {
                new VkPushConstantRange(VK_SHADER_STAGE_VERTEX_BIT,0, MemorySizeUtils.MAT4x4F)
            },
            this.sceneDescriptorset.getDescriptorSetLayout(),
            this.geometryAttachmentDescriptorset.getLayout()
        );
        this.pipelineLayout.setup();
    }

    private VkPipelineDescriptor createPipelineDescriptor( VkRenderPass renderPass ) {
        return new VkPipelineDescriptor( renderPass, 0, false, 1, true, 1, 1, 1 );
    }

    private VkVertexInputState createVertexInputState(MemoryStack stack ) {
        VkVertexInputState inputState = new VkVertexInputState(new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX)
                .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Position
                .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Normal
                .attribute( VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F ) //Texture
                .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Tangent
                .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Bitangentr
        );
        inputState.setup( stack );
        return inputState;
    }

    private void setupPipeline() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            VkPipelineDescriptor pipelineDescriptor = createPipelineDescriptor( renderpass );
            VkVertexInputState vertexInputState = createVertexInputState( stack );
            this.pipeline = new VkPipeline(
                    this.configuration,
                    this.renderer.getDevice(),
                    pipelineDescriptor,
                    this.shaderProgram,
                    this.pipelineLayout,
                    vertexInputState);
            pipeline.setup();
        }
    }

}
