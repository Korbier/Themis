package org.sc.playground.scene.cube2;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.pipeline.VkPipelineDescriptor;
import org.sc.themis.renderer.base.pipeline.VkShaderSourceCompiler;
import org.sc.themis.renderer.base.pipeline.VkVertexInputStateDescriptor;
import org.sc.themis.renderer.base.renderpass.VkRenderPass;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.material.MaterialRenderer;
import org.sc.themis.renderer.resource.material.MaterialProperties;
import org.sc.themis.renderer.resource.material.MaterialProperty;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.utils.MemorySizeUtils;

public class ColorMaterialRenderer extends MaterialRenderer {

  public static final String IDENTIFIER = "materialRenderer.color";

  public static final String VERTEX_SOURCE =
      """
            #version 450

            layout(location = 0) in vec3 position;
            layout(location = 1) in vec3 normal;
            layout(location = 2) in vec2 texture;
            layout(location = 3) in vec3 tangent;
            layout(location = 4) in vec3 bitangent;

            /******* 0 - Global Data ******************/
            layout(std140, set = 0, binding = 0) uniform Global {
                mat4 projection;
                mat4 view;
                mat4 projectionInv;
                mat4 viewInv;
                vec4 camera;
                vec2 resolution;
                uint utime;
            } global;


            /******* PUSH - Instance Data ******************/
            layout(push_constant) uniform pushConstant {
                layout( offset = 0 ) mat4 matrix;
            } instance;

            void main()
            {
                gl_Position = global.projection * global.view * instance.matrix * vec4(position, 1.0f);
            }
            """;

  public static final String FRAGMENT_SOURCE =
      """
            #version 450

            layout(location = 0) out vec4 outFragColor;

            /******* 0 - Global Data ******************/
            layout(std140, set = 0, binding = 0) uniform Global {
                mat4 projection;
                mat4 view;
                mat4 projectionInv;
                mat4 viewInv;
                vec4 camera;
                vec2 resolution;
                uint utime;
            } global;

            /******* 1 - Material ******************/
            layout(std140, set = 1, binding = 0) uniform Material {
                vec4 color;
            } materialRenderer;

            void main() {
                outFragColor = materialRenderer.color;
            }
            """;

  private static final int BUFFER_SIZE = MemorySizeUtils.VEC4F;
  private static final VkBufferDescriptor BUFFER_DESCRIPTOR =
      VkBufferDescriptor.descriptorsetUniform(BUFFER_SIZE);

  public ColorMaterialRenderer(
      Configuration configuration,
      Renderer renderer,
      VkRenderPass renderPass,
      SceneDescriptorSet sceneDescriptorSet) {

    super(configuration, renderer, IDENTIFIER);

    setVariantsIdentifierFunction(props -> props.get(MaterialProperties.COLOR_AMBIENT).toString());

    /** Pipeline * */
    addShader(
        VK_SHADER_STAGE_VERTEX_BIT,
        VkShaderSourceCompiler.compileShader(VERTEX_SOURCE, Shaderc.shaderc_glsl_vertex_shader));
    addShader(
        VK_SHADER_STAGE_FRAGMENT_BIT,
        VkShaderSourceCompiler.compileShader(
            FRAGMENT_SOURCE, Shaderc.shaderc_glsl_fragment_shader));
    addConstantRange(VK_SHADER_STAGE_VERTEX_BIT, 0, MemorySizeUtils.MAT4x4F);
    setVertexInputDescriptor(
        new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX)
            .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) // Position
            .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) // Normal
            .attribute(VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F) // Texture
            .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) // Tangent
            .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) // Bitangentr
        );
    setPipelineDescriptor(new VkPipelineDescriptor(renderPass, 0, false, 1, true, 1, 1, 1));

    /** Variant layout * */
    addVariantsUniformBinding(
        0, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, BUFFER_DESCRIPTOR);
    setVariantsUniformSetter(
        (binding, buffer, props) -> buffer.set(0, props.getProperty(MaterialProperties.COLOR_AMBIENT)));

    /** Other descriptorsets * */
    setDescriptorsetProviders(sceneDescriptorSet);
  }
}
