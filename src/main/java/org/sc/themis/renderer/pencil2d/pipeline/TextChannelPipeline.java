package org.sc.themis.renderer.pencil2d.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.pipeline.*;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.base.renderpass.VkRenderPass;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;

public class TextChannelPipeline extends TObject implements Pencil2DChannelPipeline {

  private final String VERTEX_SRC = """
            #version 450

            layout(location = 0) out vec2 outTexture;
            layout(location = 1) out vec4 outColor;
            layout(location = 2) out vec4 outProperties;

            layout(location = 0) in vec2 position;
            layout(location = 1) in vec2 textureCoord;
            layout(location = 2) in vec4 color;
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
  private final String FRAGMENT_SRC = """
            #version 450

            layout(location = 0) in vec2 inTexture;
            layout(location = 1) in vec4 inColor;
            layout(location = 2) in vec4 inProperties;

            layout(location = 0) out vec4 outFragColor;

            layout(set = 0, binding = 1) uniform sampler2DArray textureSampler;

            void main() {
                float originAlpha = texture(textureSampler, vec3(inTexture,inProperties.x)).r;
                float distance = 1.0 - originAlpha;
                float alpha = 1.0 - smoothstep(inProperties.y, inProperties.y + inProperties.z, distance);
                outFragColor = vec4(inColor.rgb, alpha);
            }
            """;
  private final Renderer renderer;
  private final VkRenderPass pass;
  private final VkDescriptorSetLayout layout;

  private VkShaderProgram shaderProgram;
  private VkPipelineLayout pipelineLayout;
  private VkPipeline pipeline;

  public TextChannelPipeline(Configuration configuration, Renderer renderer, VkRenderPass pass, VkDescriptorSetLayout layout) {
    super(configuration);
    this.renderer = renderer;
    this.pass = pass;
    this.layout = layout;
  }

  @Override
  public void setup() throws ThemisException {
    setupPipeline();
  }

  @Override
  public void cleanup() throws ThemisException {
    this.pipeline.cleanup();
    this.pipelineLayout.cleanup();
    this.shaderProgram.cleanup();
  }

  private void setupPipeline() throws ThemisException {

    VkShaderProgramStage vertexShader = new VkShaderProgramStage(
        VK_SHADER_STAGE_VERTEX_BIT,
        VkShaderSourceCompiler.compileShader(VERTEX_SRC, Shaderc.shaderc_glsl_vertex_shader)
    );

    VkShaderProgramStage fragmentShader = new VkShaderProgramStage(
        VK_SHADER_STAGE_FRAGMENT_BIT,
        VkShaderSourceCompiler.compileShader(FRAGMENT_SRC, Shaderc.shaderc_glsl_fragment_shader)
    );

    this.shaderProgram = new VkShaderProgram(getConfiguration(), this.renderer.getDevice(), vertexShader, fragmentShader);
    this.shaderProgram.setup();

    this.pipelineLayout = new VkPipelineLayout(getConfiguration(),this.renderer.getDevice(),new VkPushConstantRange[0],this.layout);
    this.pipelineLayout.setup();

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkVertexInputStateDescriptor descriptor =
          new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX)
              .attribute(VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F) // 2D position
              .attribute(VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F) // Texture
              .attribute(VK_FORMAT_R32G32B32A32_SFLOAT, MemorySizeUtils.VEC4F) // Color
              .attribute(VK_FORMAT_R32G32B32A32_SFLOAT, MemorySizeUtils.VEC4F); // Properties

      VkVertexInputState inputState = new VkVertexInputState(descriptor);
      inputState.setup(stack);

      this.pipeline = new VkPipeline(
          getConfiguration(), this.renderer.getDevice(),
          new VkPipelineDescriptor(this.pass, 0, true, 1, false, 1, 1, 1),
          this.shaderProgram, this.pipelineLayout, inputState
      );

      this.pipeline.setup();

    }
  }

  @Override
  public VkPipeline getPipeline() {
    return this.pipeline;
  }

}
