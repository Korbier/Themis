package org.sc.themis.renderer.material;

import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.exception.MaterialException;
import org.sc.themis.renderer.base.pipeline.*;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;

import java.util.ArrayList;
import java.util.List;

public class MaterialPipeline {

  private Renderer renderer;

  private final List<VkShaderProgramStage> shaderProgramStages = new ArrayList<>();
  private final List<VkPushConstantRange> pushConstantRanges = new ArrayList<>();
  private VkVertexInputStateDescriptor vertexInputStateDescriptor = null;
  private VkPipelineDescriptor pipelineDescriptor = null;

  private VkShaderProgram program;
  private VkPipelineLayout layout;
  private VkPipeline pipeline;

  public void setup(Renderer renderer, VkDescriptorSetLayout... layouts) throws ThemisException {
    this.renderer = renderer;
    setupShaderProgram();
    setupPipelineLayout(layouts);
    setupPipeline();
  }

  public void cleanup() throws ThemisException {
    this.pipeline.cleanup();
    this.layout.cleanup();
    this.program.cleanup();
  }

  public VkPipeline getPipeline() {
    return this.pipeline;
  }

  public void addShader(int shaderStage, byte[] source) {
    this.shaderProgramStages.add(new VkShaderProgramStage(shaderStage, source));
  }

  public void addConstantRange(int stage, int offset, int size) {
    this.pushConstantRanges.add(new VkPushConstantRange(stage, offset, size));
  }

  public void setVertexInputDescriptor(VkVertexInputStateDescriptor descriptor) {
    this.vertexInputStateDescriptor = descriptor;
  }

  public void setPipelineDescriptor(VkPipelineDescriptor descriptor) {
    this.pipelineDescriptor = descriptor;
  }

  private void setupPipeline() throws ThemisException {

    Assertions.notNull(this.pipelineDescriptor, new MaterialException("No Pipeline Descriptor defined (call method setPipelineDescriptor)"));
    Assertions.notNull(this.vertexInputStateDescriptor, new MaterialException("No Vertex InputState defined (call method setVertexInputDescriptor)"));

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkVertexInputState inputState = new VkVertexInputState(this.vertexInputStateDescriptor);
      inputState.setup(stack);

      this.pipeline = new VkPipeline(this.renderer.getDevice(), this.pipelineDescriptor, this.program, this.layout, inputState);

      this.pipeline.setup();
    }
  }

  private void setupShaderProgram() throws ThemisException {
    Assertions.notEmpty(this.shaderProgramStages, new MaterialException("No Shader Program Stage provided (call method addShader)"));
    this.program = new VkShaderProgram(renderer.getDevice(), this.shaderProgramStages.toArray(new VkShaderProgramStage[0]));
    this.program.setup();
  }

  private void setupPipelineLayout(VkDescriptorSetLayout... layouts) throws ThemisException {
    this.layout = new VkPipelineLayout(this.renderer.getDevice(), this.pushConstantRanges.toArray(new VkPushConstantRange[0]), layouts);
    this.layout.setup();
  }
}
