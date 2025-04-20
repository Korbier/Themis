package org.sc.viewer.renderactivity.postprocess.pipeline;

import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.pipeline.*;
import org.sc.themis.renderer.base.renderpass.VkRenderPass;
import org.sc.themis.scene.descriptorset.InputDescriptorSet;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;
import org.sc.viewer.renderactivity.postprocess.PostProcessor;

import static org.lwjgl.vulkan.VK10.*;

public class PostProcessorDrawOncePipeline implements PostProcessorPipeline {

  private final Configuration configuration;
  private final Renderer renderer;
  private final PostProcessor postProcessor;
  private SceneDescriptorSet sceneDescriptorset;
  private InputDescriptorSet geometryAttachmentDescriptorset;
  private VkRenderPass renderpass;
  private VkShaderProgram shaderProgram;
  private VkPipelineLayout pipelineLayout;
  private VkPipeline pipeline;

  public PostProcessorDrawOncePipeline(
      Configuration configuration, Renderer renderer, VkRenderPass renderpass,
      SceneDescriptorSet sceneDescriptorset, InputDescriptorSet geometryAttachmentDescriptorset,
      PostProcessor postProcessor) {
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

  public void resize(VkRenderPass renderpass, SceneDescriptorSet sceneDescriptorset, InputDescriptorSet geometryAttachmentDescriptorset)
      throws ThemisException {
    this.renderpass = renderpass;
    this.sceneDescriptorset = sceneDescriptorset;
    this.geometryAttachmentDescriptorset = geometryAttachmentDescriptorset;
    this.pipeline.cleanup();
    setupPipeline();
  }

  public void bind(VkCommand command, int frame) throws ThemisException {
    command.bindPipeline(this.pipeline);
    command.bindDescriptorSets(
        new int[0],
        this.sceneDescriptorset.getDescriptorSet(frame),
        this.geometryAttachmentDescriptorset.getDescriptorSet(frame));
  }

  private void setupShaderProgram() throws ThemisException {

    this.shaderProgram =
        new VkShaderProgram(
            this.configuration,
            this.renderer.getDevice(),
            new VkShaderProgramStage(VK_SHADER_STAGE_VERTEX_BIT, this.postProcessor.getVertexShader()),
            new VkShaderProgramStage(VK_SHADER_STAGE_GEOMETRY_BIT, this.postProcessor.getGeometryShader()),
            new VkShaderProgramStage(VK_SHADER_STAGE_FRAGMENT_BIT, this.postProcessor.getFragmentShader())
        );

    this.shaderProgram.setup();
  }

  private void setupPipelineLayout() throws ThemisException {
    this.pipelineLayout =
        new VkPipelineLayout(
            this.configuration,
            this.renderer.getDevice(),
            new VkPushConstantRange[] {
              new VkPushConstantRange(VK_SHADER_STAGE_VERTEX_BIT, 0, MemorySizeUtils.MAT4x4F)
            },
            this.sceneDescriptorset.getDescriptorSetLayout(),
            this.geometryAttachmentDescriptorset.getLayout());
    this.pipelineLayout.setup();
  }

  private VkPipelineDescriptor createPipelineDescriptor(VkRenderPass renderPass) {
    return new VkPipelineDescriptor(renderPass, 0, false, 1, true, 1, 1, 1);
  }

  private VkVertexInputState createVertexInputState(MemoryStack stack) {
    VkVertexInputState inputState = new VkVertexInputState();
    inputState.setup(stack);
    return inputState;
  }

  private void setupPipeline() throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkPipelineDescriptor pipelineDescriptor = createPipelineDescriptor(renderpass);
      VkVertexInputState vertexInputState = createVertexInputState(stack);
      this.pipeline = new VkPipeline(
          this.configuration, this.renderer.getDevice(), pipelineDescriptor,
          this.shaderProgram, this.pipelineLayout, vertexInputState
      );
      pipeline.setup();
    }
  }
}
