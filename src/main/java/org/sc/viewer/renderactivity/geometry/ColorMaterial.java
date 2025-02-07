package org.sc.viewer.renderactivity.geometry;

import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.material.Material;
import org.sc.themis.renderer.material.MaterialProperty;
import org.sc.themis.renderer.pipeline.VkPipelineDescriptor;
import org.sc.themis.renderer.pipeline.VkShaderSourceCompiler;
import org.sc.themis.renderer.pipeline.VkVertexInputStateDescriptor;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.scene.descriptorset.LightDescriptorSet;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;

public class ColorMaterial extends Material {

    public static final String IDENTIFIER = "material.color";

    public static final String VERTEX_SOURCE = """
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

    public static final String FRAGMENT_SOURCE = """
            #version 450
            
            layout(location = 0) in vec3 inPosition;
            layout(location = 1) in vec3 inNormal;
            
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
            layout(std140, set = 2, binding = 0) uniform Material {
                vec4 color;
            } material;
            
            vec3 ambient(vec3 lightAmbientColor, vec3 materialColor) {
                return lightAmbientColor * materialColor;
            }
            
            vec3 diffuseDirectional( vec3 nlNormal, vec3 lightDirection, vec3 lightDiffuseColor, vec3 materialColor ) {
                vec3 oppLightDirection  = normalize( -lightDirection );
                float diff = max( dot( nlNormal, oppLightDirection), 0.0 );
                return lightDiffuseColor * materialColor * diff;
            }
            
            vec3 directional( vec3 nlNormal, vec3 materialColor, DirectionalLight light ) {
                vec3 ambientColor = ambient( light.ambient.rgb, materialColor );
                vec3 diffuseColor = diffuseDirectional( nlNormal, light.direction.xyz, light.diffuse.rgb, materialColor );
                // vec3 specularColor = specularDirectional( fragPosition, normal, light.direction.xyz, light.specular.rgb, diffuse, light.shininess );
                return /*ambientColor +*/ diffuseColor;
            }
            
            vec3 directionals( vec3 nlNormal, vec3 materialColor) {
                vec3 color = vec3(0.0f);
                for (int i = 0; i<lights.directionalLightCount; i++ ) {
                   // if ( directionalLights.lights[i].visible.x == 1.0f ) {
                        color += directional(nlNormal, materialColor, directionalLights.lights[i]);
                   // }
                }
                return color;
            }
            
            
            void main() {
            
                vec3 nlNormal = normalize(inNormal);
                vec3 materialColor = material.color.rgb;
    
                vec3 finalColor = vec3(0.0f);
                finalColor += directional(nlNormal, materialColor, directionalLights.lights[0]);//  directionals( inPosition.xyz, nlNormal, materialColor);

                outColor = vec4( finalColor, 1.0f );

            }
            """;

    private static final int BUFFER_SIZE = MemorySizeUtils.VEC4F;
    private static final VkBufferDescriptor BUFFER_DESCRIPTOR = VkBufferDescriptor.descriptorsetUniform(BUFFER_SIZE);

    public ColorMaterial(
            Configuration configuration, Renderer renderer, VkRenderPass renderPass,
            SceneDescriptorSet sceneDescriptorSet, LightDescriptorSet lightDescriptorSet) {

        super(configuration, renderer, IDENTIFIER);

        setMaterialPropertiesValidator(props -> props.containsKey(MaterialProperty.Color.BASE));
        setVariantsIdentifierFunction(props -> props.get(MaterialProperty.Color.BASE).toString());

        addShader(VK_SHADER_STAGE_VERTEX_BIT, VkShaderSourceCompiler.compileShader(VERTEX_SOURCE, Shaderc.shaderc_glsl_vertex_shader));
        addShader(VK_SHADER_STAGE_FRAGMENT_BIT, VkShaderSourceCompiler.compileShader(FRAGMENT_SOURCE, Shaderc.shaderc_glsl_fragment_shader));
        addConstantRange(VK_SHADER_STAGE_VERTEX_BIT, 0, MemorySizeUtils.MAT4x4F);
        setVertexInputDescriptor(new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX)
                .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) //Position
                .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) //Normal
                .attribute(VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F) //Texture
                .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) //Tangent
                .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F) //Bitangentr
       );
        setPipelineDescriptor(new VkPipelineDescriptor(renderPass, 0, false, 1, true, 1, 1, 1));

        addVariantsUniformBinding(0, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, BUFFER_DESCRIPTOR );
        setVariantsUniformSetter((binding, buffer, props) -> buffer.set(0, props.getProperty(MaterialProperty.Color.BASE)));

        setDescriptorsetProviders(sceneDescriptorSet, lightDescriptorSet);

    }

}
