package org.sc.playground.scene.cube3;

import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
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
import org.sc.themis.renderer.base.resource.image.VkSamplerDescriptor;
import org.sc.themis.renderer.material.Material;
import org.sc.themis.renderer.material.MaterialProperty;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.utils.MemorySizeUtils;

public class TextureMaterial extends Material {

  public static final String IDENTIFIER = "material.texture";

  public static final String VERTEX_SOURCE =
      """
            #version 450

            layout(location = 0) in vec3 position;
            layout(location = 1) in vec3 normal;
            layout(location = 2) in vec2 texture;
            layout(location = 3) in vec3 tangent;
            layout(location = 4) in vec3 bitangent;

            layout(location = 0) out vec2 outTexture;

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
                outTexture = texture;
            }
            """;

  public static final String FRAGMENT_SOURCE =
      """
            #version 450

            layout(location = 0) in  vec2 inTexture;
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
            layout(set = 1, binding = 0) uniform sampler2D textureSampler;

            void main() {
                outFragColor = texture(textureSampler, inTexture);
            }
            """;

  public static final String MATERIAL_ID = "material.texture";
  private static final VkSamplerDescriptor DESCRIPTOR =
      new VkSamplerDescriptor(VK_FILTER_LINEAR, 1, true);

  public TextureMaterial(
      Configuration configuration,
      Renderer renderer,
      VkRenderPass renderPass,
      SceneDescriptorSet sceneDescriptorSet) {

    super(configuration, renderer, MATERIAL_ID);

    setVariantsIdentifierFunction(props -> props.get(MaterialProperty.Texture.BASE).toString());
    setMaterialPropertiesValidator(props -> props.containsKey(MaterialProperty.Texture.BASE));

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
            .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F));
    setPipelineDescriptor(new VkPipelineDescriptor(renderPass, 0, false, 1, true, 1, 1, 1));

    /** Variant layout * */
    addVariantsCombinedImageSamplerBinding(
        0, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, DESCRIPTOR);
    setVariantsCombinedImageSamplerSetter(
        (binding, descriptorset, sampler, props) ->
            descriptorset.bind(
                binding, props.getProperty(MaterialProperty.Texture.BASE).getView(), sampler));

    /** Other descriptorsets * */
    setDescriptorsetProviders(sceneDescriptorSet);
  }
}
