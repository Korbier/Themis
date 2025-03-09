package org.sc.viewer.renderactivity.geometry;

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
import org.sc.themis.renderer.material.Material;
import org.sc.themis.renderer.material.MaterialProperty;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.scene.light.pipeline.LightDescriptorSet;
import org.sc.themis.scene.light.pipeline.PhongShaderSource;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.utils.MemorySizeUtils;

public class ColorMaterial extends Material {

  public static final String IDENTIFIER = "material.color";

  public static final String VERTEX_SOURCE =
      """
            #version 450

            layout(location = 0) out vec3 outPosition;
            layout(location = 1) out vec3 outNormal;

            layout(location = 0) in vec3 inPosition;
            layout(location = 1) in vec3 inNormal;
            layout(location = 2) in vec2 inTexture;
            layout(location = 3) in vec3 inTangent;
            layout(location = 4) in vec3 inBitangent;

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
                layout(offset = 0) mat4 matrix;
            } instance;

            void main()
            {
                gl_Position = global.projection * global.view * instance.matrix * vec4(inPosition, 1.0f);
                outPosition = (instance.matrix * vec4(inPosition, 1.0f)).xyz;
                outNormal  = inNormal;
            }
            """;

  public static final String FRAGMENT_SOURCE =
      PhongShaderSource.inject(
          1,
          """
            #version 450

            layout(location = 0) in vec3 inPosition;
            layout(location = 1) in vec3 inNormal;

            layout(location = 0) out vec4 outColor;

            /******* STRUCTS - MATERIAL ******************/
            struct Material {
                vec4 ambient;
                vec4 diffuse;
                vec4 specular;
                float shininess;
            };

            /******* STRUCTS - LIGHTS ******************/
            $PHONG_LIGHT_STRUCT$

            /******* DESCRIPTORSET - 0 - Global Data ******************/
            layout(std140, set = 0, binding = 0) uniform Global {
                mat4 projection;
                mat4 view;
                mat4 projectionInv;
                mat4 viewInv;
                vec4 camera;
                vec2 resolution;
                uint utime;
            } global;

            /******* DESCRIPTORSET - 1 - Lights ******************/
            $PHONG_LIGHT_DESCRIPTORSET$

            /******* DESCRIPTORSET - 2 - Material ******************/
            layout(std140, set = 2, binding = 0) uniform MaterialUni {
                Material content;
            } material;

            /**** FUNCTIONS - Lights ****/
            $PHONG_LIGHT_FUNTIONS$

            /**** MAIN ****/
            void main() {

                Material material = material.content;
                vec3 nlNormal = normalize(inNormal);
                vec3 position = inPosition;
                vec3 finalColor = vec3(0.0f);

                finalColor += phong_directionals(nlNormal, position, material.ambient.rgb, material.diffuse.rgb, material.specular.rgb, material.shininess);
                finalColor += phong_points(nlNormal, position, material.ambient.rgb, material.diffuse.rgb, material.specular.rgb, material.shininess);
                finalColor += phong_spots(nlNormal, position, material.ambient.rgb, material.diffuse.rgb, material.specular.rgb, material.shininess);

                outColor = vec4( finalColor, 1.0f );

            }
            """);

  private static final int BUFFER_SIZE =
      MemorySizeUtils.VEC4F // Ambient component
          + MemorySizeUtils.VEC4F // Diffuse component
          + MemorySizeUtils.VEC4F // Specular component
          + MemorySizeUtils.FLOAT; // Shininess
  private static final VkBufferDescriptor BUFFER_DESCRIPTOR =
      VkBufferDescriptor.descriptorsetUniform(BUFFER_SIZE);

  public ColorMaterial(
      Configuration configuration,
      Renderer renderer,
      VkRenderPass renderPass,
      SceneDescriptorSet sceneDescriptorSet,
      LightDescriptorSet lightDescriptorSet) {

    super(configuration, renderer, IDENTIFIER);

    setMaterialPropertiesValidator(
        props ->
            props.containsKeys(
                MaterialProperty.Color.BASE,
                MaterialProperty.Color.DIFFUSE,
                MaterialProperty.Color.SPECULAR,
                MaterialProperty.Property.SHININESS));

    setVariantsIdentifierFunction(
        props ->
            props.generateVariantIdentifier(
                MaterialProperty.Color.BASE,
                MaterialProperty.Color.DIFFUSE,
                MaterialProperty.Color.SPECULAR,
                MaterialProperty.Property.SHININESS));

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

    addVariantsUniformBinding(
        0, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, BUFFER_DESCRIPTOR);
    setVariantsUniformSetter(
        (binding, buffer, props) -> {
          buffer.set(0, props.getProperty(MaterialProperty.Color.BASE));
          buffer.set(MemorySizeUtils.VEC4F, props.getProperty(MaterialProperty.Color.DIFFUSE));
          buffer.set(
              MemorySizeUtils.VEC4F + MemorySizeUtils.VEC4F,
              props.getProperty(MaterialProperty.Color.SPECULAR));
          buffer.set(
              MemorySizeUtils.VEC4F + MemorySizeUtils.VEC4F + MemorySizeUtils.VEC4F,
              props.getProperty(MaterialProperty.Property.SHININESS));
        });

    setDescriptorsetProviders(sceneDescriptorSet, lightDescriptorSet);
  }
}
