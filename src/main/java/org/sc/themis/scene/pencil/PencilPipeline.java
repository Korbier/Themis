package org.sc.themis.scene.pencil;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.pipeline.VkPipeline;
import org.sc.themis.renderer.base.pipeline.VkPipelineDescriptor;
import org.sc.themis.renderer.base.pipeline.VkPipelineLayout;
import org.sc.themis.renderer.base.pipeline.VkPushConstantRange;
import org.sc.themis.renderer.base.pipeline.VkShaderProgram;
import org.sc.themis.renderer.base.pipeline.VkShaderProgramStage;
import org.sc.themis.renderer.base.pipeline.VkShaderSourceCompiler;
import org.sc.themis.renderer.base.pipeline.VkVertexInputState;
import org.sc.themis.renderer.base.pipeline.VkVertexInputStateDescriptor;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.renderpass.VkRenderPass;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;

public class PencilPipeline extends TObject {

  private final String VERTEX_SRC =
      """
            #version 450

            layout(location = 0) out vec2 outTexture;
            layout(location = 1) out vec3 outColor;
            layout(location = 2) out vec4 outProperties;

            layout(location = 0) in vec2 position;
            layout(location = 1) in vec2 textureCoord;
            layout(location = 2) in vec3 color;
            layout(location = 3) in vec4 properties;

            layout(set = 0, binding = 0) uniform Global {
                mat4 projection;
                float fov;
                float znear;
                float zfar;
                vec2 resolution;
            } global;

            void main()
            {
                outTexture = textureCoord;
                outProperties = properties;
                outColor = color;
                gl_Position = global.projection * vec4(position, global.znear * -1, 1.0f);
            }
            """;
  private final String FRAGMENT_SRC =
      """
            #version 450

            layout(location = 0) in vec2 inTexture;
            layout(location = 1) in vec3 inColor;
            layout(location = 2) in vec4 inProperties;

            layout(location = 0) out vec4 outFragColor;

            layout(set = 0, binding = 1) uniform sampler2DArray textureSampler;

            void main() {
                if (inProperties.x == 1.0f) {
                  float originAlpha = texture(textureSampler, vec3(inTexture, inProperties.y)).a;
                  float distance = 1.0 - originAlpha;
                  float alpha = 1.0 - smoothstep(inProperties.z, inProperties.z + inProperties.w, distance);
                  outFragColor = vec4(pow(inColor.rgb, vec3(2.2)), alpha);
                } else {
                  outFragColor = vec4(pow(inColor.rgb, vec3(2.2)), 1.0f);//vec4(inColor, 1.0f);//
                }
            }
            """;

  private final Renderer renderer;
  private final VkRenderPass pass;
  private final Pencil pencil;

  private VkShaderProgram shaderProgram;
  private VkPipelineLayout pipelineLayout;
  private VkPipeline pipeline;

  private PencilDescriptorSet pencilDescriptorSet;

  private VkBuffer drawCommandVertexBuffer;
  private VkBuffer drawCommandIndiceBuffer;

  public PencilPipeline(
      Configuration configuration, Renderer renderer, VkRenderPass pass, Pencil pencil) {
    super(configuration);
    this.renderer = renderer;
    this.pass = pass;
    this.pencil = pencil;
  }

  @Override
  public void setup() throws ThemisException {
    setupDescriptorset();
    setupPipeline();
  }

  private void setupDescriptorset() throws ThemisException {
    this.pencilDescriptorSet =
        new PencilDescriptorSet(getConfiguration(), this.renderer, this.pencil);
    this.pencilDescriptorSet.setup();
  }

  @Override
  public void cleanup() throws ThemisException {
    if (this.drawCommandVertexBuffer != null) {
      this.drawCommandVertexBuffer.cleanup();
    }
    if (this.drawCommandIndiceBuffer != null) {
      this.drawCommandIndiceBuffer.cleanup();
    }
    this.pipeline.cleanup();
    this.pipelineLayout.cleanup();
    this.shaderProgram.cleanup();
    this.pencilDescriptorSet.cleanup();
  }

  public void draw(VkCommand command, int frame) throws ThemisException {
    if (this.pencil != null && this.pencil.isRenderable()) {
      this.updateBuffers();
      command.bindPipeline(this.getPipeline());
      command.bindDescriptorSets(new int[0], this.getDescriptorset(frame));
      command.bindBuffers(this.drawCommandVertexBuffer, this.drawCommandIndiceBuffer);
      command.drawIndexed(this.pencil.getIndiceSize());
    }
  }

  public VkPipeline getPipeline() {
    return this.pipeline;
  }

  public void update(Scene scene) throws ThemisException {
    this.pencilDescriptorSet.updateAll(scene);
  }

  private void updateBuffers() throws ThemisException {

    long dataSize = (long) pencil.getDataSize() * MemorySizeUtils.FLOAT;
    long indiceSize = (long) pencil.getIndiceSize() * MemorySizeUtils.INT;

    if (this.drawCommandVertexBuffer == null
        || this.drawCommandVertexBuffer.getRequestedSize() < dataSize) {

      if (this.drawCommandVertexBuffer != null) {
        this.drawCommandVertexBuffer.cleanup();
      }

      VkBufferDescriptor decriptor = VkBufferDescriptor.vertexBuffer(dataSize);
      this.drawCommandVertexBuffer =
          new VkBuffer(
              getConfiguration(),
              this.renderer.getDevice(),
              this.renderer.getMemoryAllocator(),
              decriptor);
      this.drawCommandVertexBuffer.setup();
    }

    this.drawCommandVertexBuffer.set(0, pencil.getData());

    if (this.drawCommandIndiceBuffer == null
        || this.drawCommandIndiceBuffer.getRequestedSize() < indiceSize) {

      if (this.drawCommandIndiceBuffer != null) {
        this.drawCommandIndiceBuffer.cleanup();
      }

      VkBufferDescriptor decriptorIndices = VkBufferDescriptor.indiceBuffer(indiceSize);
      this.drawCommandIndiceBuffer =
          new VkBuffer(
              getConfiguration(),
              this.renderer.getDevice(),
              this.renderer.getMemoryAllocator(),
              decriptorIndices);
      this.drawCommandIndiceBuffer.setup();
    }

    this.drawCommandIndiceBuffer.set(0, pencil.getIndices());
  }

  public VkDescriptorSet getDescriptorset(int frame) {
    return this.pencilDescriptorSet.getDescriptorSet(frame);
  }

  private void setupPipeline() throws ThemisException {

    VkShaderProgramStage vertexShader =
        new VkShaderProgramStage(
            VK_SHADER_STAGE_VERTEX_BIT,
            VkShaderSourceCompiler.compileShader(VERTEX_SRC, Shaderc.shaderc_glsl_vertex_shader));

    VkShaderProgramStage fragmentShader =
        new VkShaderProgramStage(
            VK_SHADER_STAGE_FRAGMENT_BIT,
            VkShaderSourceCompiler.compileShader(
                FRAGMENT_SRC, Shaderc.shaderc_glsl_fragment_shader));

    this.shaderProgram =
        new VkShaderProgram(
            getConfiguration(), this.renderer.getDevice(), vertexShader, fragmentShader);
    this.shaderProgram.setup();

    this.pipelineLayout =
        new VkPipelineLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            new VkPushConstantRange[0],
            this.pencilDescriptorSet.getDescriptorSetLayout());
    this.pipelineLayout.setup();

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkVertexInputStateDescriptor descriptor =
          new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX)
              .attribute(VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F) // 2D position
              .attribute(VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F) // Texture
              .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) // Color
              .attribute(VK_FORMAT_R32G32B32A32_SFLOAT, MemorySizeUtils.VEC4F); // Use Texture

      VkVertexInputState inputState = new VkVertexInputState(descriptor);
      inputState.setup(stack);

      this.pipeline =
          new VkPipeline(
              getConfiguration(),
              this.renderer.getDevice(),
              new VkPipelineDescriptor(this.pass, 0, true, 1, false, 1, 1, 1),
              this.shaderProgram,
              this.pipelineLayout,
              inputState);
      this.pipeline.setup();
    }
  }
}
