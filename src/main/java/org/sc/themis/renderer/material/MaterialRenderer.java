package org.sc.themis.renderer.material;

import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.exception.MaterialException;
import org.sc.themis.renderer.base.pipeline.*;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;

import java.util.ArrayList;
import java.util.List;

public class MaterialRenderer<P extends MaterialRendererProperties> {

  private static final int DESCRIPTORPOOL_SIZE = 10;

  private String identifier;
  private P properties;

  /** Pipeline **/
  private final List<VkShaderProgramStage> shaderStages = new ArrayList<>();
  private final List<VkPushConstantRange> pushConstantRanges = new ArrayList<>();
  private VkVertexInputStateDescriptor vertexInputStateDescriptor = null;
  private VkPipelineDescriptor pipelineDescriptor = null;
  private VkDescriptorSetLayout[] layouts;
  private VkShaderProgram program;
  private VkPipelineLayout layout;
  private VkPipeline pipeline;

  /** Main variant **/
  private MaterialVariantLayout mainLayout;
  private VkDescriptorSetLayout mainDescriptorSetLayout;
  private VkDescriptorPool mainDescriptorPool;
  private MaterialVariant<P> mainVariant;
  private MaterialVariantSetter<P> mainConsumer;

  /** Material variant **/
  private MaterialVariantLayout variantLayout;
  private VkDescriptorSetLayout variantDescriptorSetLayout;
  private VkDescriptorPool variantDescriptorPool;
  private MaterialVariantSetter<Material> variantConsumer;

  public static <T extends MaterialRendererProperties> MaterialRenderer.Builder<T> builder() {
    return new MaterialRenderer.Builder<>();
  }

  public MaterialVariant<P> mainVariant() {
    return this.mainVariant;
  }

  public void set(Renderer renderer, P properties) throws ThemisException {
    if (this.mainVariant != null) {
      this.mainVariant.set(renderer, properties);
    }
  }

  public MaterialVariant<Material> create(Renderer renderer, Material initialValue) throws ThemisException {
    MaterialVariant<Material> variant = new MaterialVariant<>(this.variantLayout, this.variantDescriptorPool, this.variantConsumer);
    variant.set(renderer, initialValue);
    return variant;
  }

  public void setup(Renderer renderer) throws ThemisException {
    setupShaderPrograms(renderer);
    setupPipelineLayout(renderer);
    setupPipeline(renderer);
    setupMain(renderer);
    setupVariant(renderer);
  }

  public void cleanup() throws ThemisException {

    if (this.variantDescriptorSetLayout != null) {
      this.variantDescriptorPool.cleanup();
      this.variantDescriptorSetLayout.cleanup();
    }

    if (this.mainDescriptorSetLayout != null) {
      this.mainDescriptorPool.cleanup();
      this.mainDescriptorSetLayout.cleanup();
    }

    this.pipeline.cleanup();
    this.layout.cleanup();
    this.program.cleanup();

  }

  private void setupShaderPrograms(Renderer renderer) throws ThemisException {
    Assertions.notEmpty(this.shaderStages, new MaterialException("No Shader Program Stage provided (call method addShader)"));
    this.program = new VkShaderProgram(renderer.getDevice(), this.shaderStages.toArray(new VkShaderProgramStage[0]));
    this.program.setup();
  }

  private void setupPipelineLayout(Renderer renderer) throws ThemisException {
    this.layout = new VkPipelineLayout(renderer.getDevice(), this.pushConstantRanges.toArray(new VkPushConstantRange[0]), layouts);
    this.layout.setup();
  }

  private void setupPipeline(Renderer renderer) throws ThemisException {

    Assertions.notNull(this.pipelineDescriptor, new MaterialException("No Pipeline Descriptor defined (call method setPipelineDescriptor)"));
    Assertions.notNull(this.vertexInputStateDescriptor, new MaterialException("No Vertex InputState defined (call method setVertexInputDescriptor)"));

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkVertexInputState inputState = new VkVertexInputState(this.vertexInputStateDescriptor);
      inputState.setup(stack);

      this.pipeline = new VkPipeline(renderer.getDevice(), this.pipelineDescriptor, this.program, this.layout, inputState);

      this.pipeline.setup();
    }

  }

  private void setupMain(Renderer renderer) throws ThemisException {

    VkDescriptorSetBinding[] bindings = this.mainLayout.bindings().values().toArray(new VkDescriptorSetBinding[0]);

    if (bindings.length > 0) {
      this.mainDescriptorSetLayout = new VkDescriptorSetLayout(renderer.getDevice(), bindings);
      this.mainDescriptorSetLayout.setup();
    }

    if (this.mainDescriptorSetLayout != null) {

      this.mainDescriptorPool = new VkDescriptorPool(renderer.getDevice(), renderer.getFramesInFlight().getSize(), this.mainDescriptorSetLayout);
      this.mainDescriptorPool.setup();

      this.mainVariant = new MaterialVariant<>(this.mainLayout, this.mainDescriptorPool, this.mainConsumer);

    }

  }

  private void setupVariant(Renderer renderer) throws ThemisException {

    VkDescriptorSetBinding[] bindings = this.variantLayout.bindings().values().toArray(new VkDescriptorSetBinding[0]);

    if (bindings.length > 0) {
      this.variantDescriptorSetLayout = new VkDescriptorSetLayout(renderer.getDevice(), bindings);
      this.variantDescriptorSetLayout.setup();
    }

    if (this.variantDescriptorSetLayout != null) {
      this.variantDescriptorPool = new VkDescriptorPool(renderer.getDevice(), renderer.getFramesInFlight().getSize() * DESCRIPTORPOOL_SIZE, this.variantDescriptorSetLayout);
      this.variantDescriptorPool.setup();
    }

  }

  public static class Builder<P extends MaterialRendererProperties> {

    private final MaterialRenderer<P> mRenderer;

    public Builder() {
      this.mRenderer = new MaterialRenderer<>();
    }

    public MaterialRenderer.Builder<P> identifier(String identifier) {
      this.mRenderer.identifier = identifier;
      return this;
    }

    public MaterialRenderer.Builder<P> shaderStage(int shaderStage, byte[] source) {
      this.mRenderer.shaderStages.add(new VkShaderProgramStage(shaderStage, source));
      return this;
    }

    public MaterialRenderer.Builder<P> pushConstantRange(int stage, int offset, int  range) {
      this.mRenderer.pushConstantRanges.add(new VkPushConstantRange(stage, offset, range));
      return this;
    }

    public MaterialRenderer.Builder<P> vertexInputStateDescriptor(VkVertexInputStateDescriptor descriptor) {
      this.mRenderer.vertexInputStateDescriptor = descriptor;
      return this;
    }

    public MaterialRenderer.Builder<P> pipelineDescriptor(VkPipelineDescriptor descriptor) {
      this.mRenderer.pipelineDescriptor = descriptor;
      return this;
    }

    public MaterialRenderer.Builder<P> layouts(VkDescriptorSetLayout... layouts) {
      this.mRenderer.layouts = layouts;
      return this;
    }

    public MaterialRenderer.Builder<P> mainLayout(MaterialVariantLayout layout) {
      this.mRenderer.mainLayout = layout;
      return this;
    }

    public MaterialRenderer.Builder<P> variantLayout(MaterialVariantLayout layout) {
      this.mRenderer.variantLayout = layout;
      return this;
    }

    public MaterialRenderer.Builder<P> mainConsumer(MaterialVariantSetter<P> consumer) {
      this.mRenderer.mainConsumer = consumer;
      return this;
    }

    public MaterialRenderer.Builder<P> variantConsumer(MaterialVariantSetter<Material> consumer) {
      this.mRenderer.variantConsumer = consumer;
      return this;
    }

    public MaterialRenderer<P> build() {
      return this.mRenderer;
    }

  }


}
