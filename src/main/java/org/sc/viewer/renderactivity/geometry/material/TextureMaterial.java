package org.sc.viewer.renderactivity.geometry.material;

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
import org.sc.themis.scene.light.pipeline.LightDescriptorSet;
import org.sc.themis.scene.light.pipeline.PhongShaderSource;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.utils.MemorySizeUtils;

public class TextureMaterial extends Material {

  public static final String VERTEX_SOURCE =
      """
            #version 450

            layout(location = 0) out vec2 outTexture;
            layout(location = 1) out vec3 outPosition;
            layout(location = 2) out vec3 outNormal;

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
                outTexture  = inTexture;
                outPosition = (instance.matrix * vec4(inPosition, 1.0f)).xyz;
                outNormal = mat3(transpose(inverse(instance.matrix))) * inNormal;

            }
            """;

  public static final String FRAGMENT_SOURCE =
      PhongShaderSource.inject( 1,
      """
            #version 450

            layout(location = 0) in vec2 inTexture;
            layout(location = 1) in vec3 inPosition;
            layout(location = 2) in vec3 inNormal;
            
            layout(location = 0) out vec4 outColor;

            /******* STRUCTS - LIGHTS ******************/
             struct DirectionalLight {
                 vec4 ambient;
                 vec4 diffuse;
                 vec4 specular;
                 vec4 data;
                 vec4 position;
             };
  
             struct PointLight {
                 vec4 ambient;
                 vec4 diffuse;
                 vec4 specular;
                 vec4 data;
                 vec4 position;
                 vec4 attenuation;
             };
  
             struct SpotLight {
                 vec4 ambient;
                 vec4 diffuse;
                 vec4 specular;
                 vec4 data;
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

            /******* DESCRIPTORSET - 1 - Lights ******************/
            layout(std140, set = 1, binding = 0) uniform Lights {
                   float directionalLightCount;
                   float pointLightCount;
                   float spotLightCount;
                   float pad;
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

            /******* DESCRIPTORSET - 2 - Material ******************/
            layout(set = 2, binding = 0) uniform sampler2D baseSampler;
            layout(set = 2, binding = 1) uniform sampler2D normalSampler;

            /**** FUNCTIONS - Lights ****/
                float attenuationType1( vec3 fragPosition, vec3 lightPosition, float radius, float falloff ) {
                   float distance = length( lightPosition - fragPosition );
                   float s = distance / radius;
                   if (s >= 1.0) return 0.0;
                   return (1 - s * s) * (1 - s * s) / (1 + falloff * s);
               }

               float attenuationType2( vec3 fragPosition, vec3 lightPosition, float radius, float falloff ) {
                   float distance = length( lightPosition - fragPosition );
                   float s = distance / radius;
                   if (s >= 1.0) return 0.0;
                   return (1 - s * s) + (1 - s * s) / (1 + falloff * s * s);
               }

               float attenuation( vec3 fragPosition, vec3 normal, vec3 lightPosition, vec4 attenuation ) {

                   if ( attenuation.x == 1.0f ) {
                       return attenuationType1( fragPosition, lightPosition, attenuation.y, attenuation.z );
                   }

                   if ( attenuation.x == 2.0f ) {
                       return attenuationType2( fragPosition, lightPosition, attenuation.y, attenuation.z );
                   }

                   return 1.0f;

               }

               vec3 ambient(vec3 lightAmbientColor, vec3 materialColor) {
                   return lightAmbientColor * materialColor;
               }

               vec3 diffuse( vec3 fragPosition, vec3 nlNormal, vec3 materialColor, vec3 lightDiffuseColor, vec3 lightPosition ) {
                   vec3 lightDirection  = normalize(lightPosition - fragPosition);
                   float diff = max( dot( nlNormal, lightDirection), 0.0 );
                   return lightDiffuseColor * materialColor * diff;
               }

               vec3 specular( vec3 fragPosition, vec3 normal, vec3 materialSpecular, float materialShininess, vec3 lightSpecularColor, vec3 lightPosition ) {

                   vec3 lightDirection  = normalize( lightPosition - fragPosition );
                   vec3 viewDirection = normalize( global.camera.xyz - fragPosition );
                   vec3 reflectDirection = reflect( -lightDirection, normal );

                   float specularFactor = max(dot(viewDirection, reflectDirection), 0.0);

                   //https://stackoverflow.com/questions/37051358/opengl-es-2-0-specular-light-generates-black-border
                   if ( specularFactor > 0.0 ) {
                       float spec = pow(specularFactor, materialShininess);
                       return lightSpecularColor * spec * materialSpecular;
                   } else {
                       return vec3(0.0f);
                   }

               }

               vec3 directional( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess, DirectionalLight light ) {
                   vec3 ambientColor = ambient( light.ambient.rgb, materialAmbient );
                   vec3 diffuseColor = diffuse( position, nlNormal, materialDiffuse, light.diffuse.rgb, light.position.xyz );
                   vec3 specularColor = specular( position, nlNormal, materialSpecular, materialShininess, light.specular.rgb, light.position.xyz );
                   return ambientColor + diffuseColor + specularColor;
               }

               vec3 point( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess, PointLight light ) {
                   vec3 ambientColor = ambient( light.ambient.rgb, materialAmbient );
                   vec3 diffuseColor = diffuse( position, nlNormal, materialDiffuse, light.diffuse.rgb, light.position.xyz );
                   vec3 specularColor = specular( position, nlNormal, materialSpecular, materialShininess, light.specular.rgb, light.position.xyz );
                   float attenuation = attenuation(position, nlNormal, light.position.xyz, light.attenuation);
                   return attenuation * (ambientColor + diffuseColor + specularColor);
               }

               float spotIntensity( vec3 fragPosition, SpotLight light ) {
                   float theta = dot(normalize(light.position.xyz - fragPosition), normalize(-light.direction.xyz));
                   float epsilon = light.innerCutOff - light.outerCutOff;
                   return clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0 );
               }

               vec3 spot( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess, SpotLight light ) {

                   vec3 ambientColor = ambient( light.ambient.rgb, materialAmbient );
                   vec3 diffuseColor = diffuse( position, nlNormal, materialDiffuse, light.diffuse.rgb, light.position.xyz );
                   vec3 specularColor = specular( position, nlNormal, materialSpecular, materialShininess, light.specular.rgb, light.position.xyz );
                   float intensity = spotIntensity(position, light);

                   diffuseColor *= intensity;
                   specularColor *= intensity;

                   float attenuation = attenuation(position, nlNormal, light.position.xyz, light.attenuation);
                   return attenuation * (ambientColor + diffuseColor + specularColor);
               }

               vec3 phong_directionals( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess ) {
                   vec3 color = vec3(0.0f);
                   for (int i = 0; i<lights.directionalLightCount; i++ ) {
                       if ( directionalLights.lights[i].data.x == 1.0f ) {
                           color += directional(nlNormal, position, materialAmbient, materialDiffuse, materialSpecular, materialShininess, directionalLights.lights[i]);
                       }
                   }
                   return color;
               }

               vec3 phong_points( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess ) {
                   vec3 color = vec3(0.0f);
                   for (int i = 0; i<lights.pointLightCount; i++ ) {
                       if ( pointLights.lights[i].data.x == 1.0f ) {
                           color += point(nlNormal, position, materialAmbient, materialDiffuse, materialSpecular, materialShininess, pointLights.lights[i]);
                       }
                   }
                   return color;
               }

               vec3 phong_spots( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess ) {
                   vec3 color = vec3(0.0f);
                   for (int i = 0; i<lights.spotLightCount; i++ ) {
                       if ( spotLights.lights[i].data.x == 1.0f ) {
                           color += spot(nlNormal, position, materialAmbient, materialDiffuse, materialSpecular, materialShininess, spotLights.lights[i]);
                       }
                   }
                   return color;
               }
            
            /**** MAIN ****/
            void main() {
            
                vec3 position = inPosition;
                vec3 nlNormal = normalize(inNormal);
                vec3 baseColor = texture(baseSampler, inTexture).rgb;
                
                float shininess = 128.0f;
                vec3 finalColor = vec3(0.0f);

                finalColor += phong_directionals(nlNormal, position, baseColor, baseColor, baseColor, shininess);
                finalColor += phong_points(nlNormal, position, baseColor, baseColor, baseColor, shininess);
                finalColor += phong_spots(nlNormal, position, baseColor, baseColor, baseColor, shininess);

                outColor = vec4( finalColor, 1.0f );

            }
            """);

  public static final String MATERIAL_ID = "material.texture";
  private static final VkSamplerDescriptor DESCRIPTOR = new VkSamplerDescriptor(VK_FILTER_LINEAR, 1, true);

  public TextureMaterial(
      Configuration configuration,
      Renderer renderer,
      VkRenderPass renderPass,
      SceneDescriptorSet sceneDescriptorSet,
      LightDescriptorSet lightDescriptorSet) {

    super(configuration, renderer, MATERIAL_ID);

    setMaterialPropertiesValidator(props -> props.containsKey(MaterialProperty.Texture.BASE));
    setVariantsIdentifierFunction(props -> props.get(MaterialProperty.Texture.BASE).toString());

    /** Pipeline * */
    addShader(VK_SHADER_STAGE_VERTEX_BIT, VkShaderSourceCompiler.compileShader(VERTEX_SOURCE, Shaderc.shaderc_glsl_vertex_shader));
    addShader(VK_SHADER_STAGE_FRAGMENT_BIT, VkShaderSourceCompiler.compileShader(FRAGMENT_SOURCE, Shaderc.shaderc_glsl_fragment_shader));
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
    addVariantsCombinedImageSamplerBinding(0, VK_SHADER_STAGE_FRAGMENT_BIT, DESCRIPTOR);
    addVariantsCombinedImageSamplerBinding(1, VK_SHADER_STAGE_FRAGMENT_BIT, DESCRIPTOR);
    setVariantsCombinedImageSamplerSetter(
        (binding, descriptorset, sampler, props) -> {
          switch (binding) {
            case 0 -> descriptorset.bind(binding, props.getProperty(MaterialProperty.Texture.BASE).getView(), sampler);
            case 1 -> descriptorset.bind(binding, props.getProperty(MaterialProperty.Texture.NORMALS).getView(), sampler);
          }
        });

    /** Other descriptorsets * */
    setDescriptorsetProviders(sceneDescriptorSet, lightDescriptorSet);
  }
}
