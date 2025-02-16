package org.sc.viewer.renderactivity.geometry;

import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.material.Material;
import org.sc.themis.renderer.material.MaterialProperty;
import org.sc.themis.renderer.pipeline.VkPipelineDescriptor;
import org.sc.themis.renderer.pipeline.VkShaderSourceCompiler;
import org.sc.themis.renderer.pipeline.VkVertexInputStateDescriptor;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.renderer.resource.image.VkSamplerDescriptor;
import org.sc.themis.scene.light.pipeline.LightDescriptorSet;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;

public class TextureMaterial extends Material {

    public static final String IDENTIFIER = "material.texture";

    public static final String VERTEX_SOURCE = """
            #version 450
            
            layout(location = 0) out vec2 outTexture;
            layout(location = 1) out vec3 outPosition;
            layout(location = 2) out mat3 outTBNMatrix;
            
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
            
            vec3 _normalize(mat3 normalMatrix, vec3 toNormalize) {
                return normalize(normalMatrix * toNormalize);
            }
            
            void main()
            {
            
                gl_Position = global.projection * global.view * instance.matrix * vec4(inPosition, 1.0f);
            
                outTexture  = inTexture;
                outPosition = (instance.matrix * vec4(inPosition, 1.0f)).xyz;
            
                mat3 normalMatrix = mat3(transpose(inverse(instance.matrix)));
                vec3 T = _normalize(normalMatrix, inTangent);
                vec3 B = _normalize(normalMatrix, inBitangent);
                vec3 N = _normalize(normalMatrix, inNormal);
        
                outTBNMatrix = mat3(T, B, N);

            }
            """;

    public static final String FRAGMENT_SOURCE = """
            #version 450
            
            layout(location = 0) in vec2 inTexture;
            layout(location = 1) in vec3 inPosition;
            layout(location = 2) in mat3 inTBNMatrix;
            
            layout(location = 0) out vec4 outColor;
            
            struct DirectionalLight {
                vec4 ambient;
                vec4 diffuse;
                vec4 specular;
                vec4 visible;
                vec4 direction;
            };
            struct PointLight {
                vec4 ambient;
                vec4 diffuse;
                vec4 specular;
                vec4 visible;
                vec4 position;
                vec4 attenuation;
            };
            struct SpotLight {
                vec4 ambient;
                vec4 diffuse;
                vec4 specular;
                vec4 visible;
                vec4 position;
                vec4 direction;
                vec4 attenuation;
                float innerCutOff; //cos(rad(angle))
                float outerCutOff; //cos(rad(angle))
            };
            
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
            
            /******* 1 - Lights ******************/
            layout(std140, set = 1, binding = 0) uniform Lights {
                uint directionalLightCount;
                uint pointLightCount;
                uint spotLightCount;
            } lights;
            
            layout(std430, set = 1, binding = 1) readonly buffer DirectionalLights {
                DirectionalLight lights[];
            } directionalLights;
            
            layout(std430, set = 1, binding = 2) readonly buffer PointLights {
                PointLight lights[];
            } pointLights;
            
            layout(std430, set = 1, binding = 3) readonly buffer SpotLights {
                SpotLight lights[];
            } spotLights;
            
            /******* 2 - Material ******************/
            layout(set = 2, binding = 0) uniform sampler2D baseSampler;
            layout(set = 2, binding = 1) uniform sampler2D normalSampler;
            
            void main() {
                outColor = texture(baseSampler, inTexture);
            }
            """;

    public  static final String MATERIAL_ID = "material.texture";
    private static final VkSamplerDescriptor DESCRIPTOR = new VkSamplerDescriptor(VK_FILTER_LINEAR, 1, true);

    public TextureMaterial(
            Configuration configuration, Renderer renderer, VkRenderPass renderPass,
            SceneDescriptorSet sceneDescriptorSet, LightDescriptorSet lightDescriptorSet) {

        super(configuration, renderer, MATERIAL_ID);

        setMaterialPropertiesValidator(props -> props.containsKey(MaterialProperty.Texture.BASE));
        setVariantsIdentifierFunction(props -> props.get(MaterialProperty.Texture.BASE).toString());

        /** Pipeline **/
        addShader(VK_SHADER_STAGE_VERTEX_BIT, VkShaderSourceCompiler.compileShader(VERTEX_SOURCE, Shaderc.shaderc_glsl_vertex_shader));
        addShader(VK_SHADER_STAGE_FRAGMENT_BIT, VkShaderSourceCompiler.compileShader(FRAGMENT_SOURCE, Shaderc.shaderc_glsl_fragment_shader));
        addConstantRange(VK_SHADER_STAGE_VERTEX_BIT, 0, MemorySizeUtils.MAT4x4F);
        setVertexInputDescriptor(new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX)
                .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) //Position
                .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) //Normal
                .attribute(VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F) //Texture
                .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) //Tangent
                .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F)
       );
        setPipelineDescriptor(new VkPipelineDescriptor(renderPass, 0, false, 1, true, 1, 1, 1));

        /** Variant layout **/
        addVariantsCombinedImageSamplerBinding(0, VK_SHADER_STAGE_FRAGMENT_BIT, DESCRIPTOR);
        addVariantsCombinedImageSamplerBinding(1, VK_SHADER_STAGE_FRAGMENT_BIT, DESCRIPTOR);
        setVariantsCombinedImageSamplerSetter((binding, descriptorset, sampler, props) -> {
            switch (binding) {
                case 0 -> descriptorset.bind(binding, props.getProperty(MaterialProperty.Texture.BASE).getView(), sampler);
                case 1 -> descriptorset.bind(binding, props.getProperty(MaterialProperty.Texture.NORMALS).getView(), sampler);
            }
        });

        /** Other descriptorsets **/
        setDescriptorsetProviders(sceneDescriptorSet, lightDescriptorSet);

    }

}
