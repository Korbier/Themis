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
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;

public class ColorMaterial extends Material {

    public final static String IDENTIFIER = "material.color";

    public final static String VERTEX_SOURCE = """
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
                layout( offset = 0 ) mat4 matrix;
            } instance;
            
            vec3 _normalize( mat3 normalMatrix, vec3 toNormalize ) {
                return normalize(normalMatrix * toNormalize);
            }
            
            void main()
            {
                gl_Position = global.projection * global.view * instance.matrix * vec4(inPosition, 1.0f);
            
                outTexture  = inTexture;
                outPosition = (instance.matrix * vec4(inPosition, 1.0f)).xyz;
            
                mat3 normalMatrix = mat3( transpose( inverse( instance.matrix ) ) );
                vec3 T = _normalize( normalMatrix, inTangent );
                vec3 B = _normalize( normalMatrix, inBitangent );
                vec3 N = _normalize( normalMatrix, inNormal );
        
                outTBNMatrix = mat3(T, B, N);
            
            }
            """;

    public final static String FRAGMENT_SOURCE = """
            #version 450
            
            layout(location = 0) in vec2 inTexture;
            layout(location = 1) in vec3 inPosition;
            layout(location = 2) in mat3 inTBNMatrix;
            
            layout(location = 0) out vec4 outColor;
            
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
            } material;
            
            void main() {
                outColor = material.color;
            }
            """;

    private final static int BUFFER_SIZE = MemorySizeUtils.VEC4F;
    private final static VkBufferDescriptor BUFFER_DESCRIPTOR = VkBufferDescriptor.descriptorsetUniform( BUFFER_SIZE );

    public ColorMaterial(Configuration configuration, Renderer renderer, VkRenderPass renderPass, SceneDescriptorSet sceneDescriptorSet) {

        super(configuration, renderer, IDENTIFIER);

        setMaterialPropertiesValidator( props -> props.containsKey(MaterialProperty.Color.BASE) );
        setVariantsIdentifierFunction( props -> props.get(MaterialProperty.Color.BASE).toString() );

        addShader( VK_SHADER_STAGE_VERTEX_BIT, VkShaderSourceCompiler.compileShader(VERTEX_SOURCE, Shaderc.shaderc_glsl_vertex_shader));
        addShader( VK_SHADER_STAGE_FRAGMENT_BIT, VkShaderSourceCompiler.compileShader(FRAGMENT_SOURCE, Shaderc.shaderc_glsl_fragment_shader));
        addConstantRange( VK_SHADER_STAGE_VERTEX_BIT, 0, MemorySizeUtils.MAT4x4F );
        setVertexInputDescriptor( new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX)
                .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Position
                .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Normal
                .attribute( VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F ) //Texture
                .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Tangent
                .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Bitangentr
        );
        setPipelineDescriptor( new VkPipelineDescriptor(renderPass, 0, false, 1, true, 1, 1, 1) );

        addVariantsUniformBinding(0, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, BUFFER_DESCRIPTOR  );
        setVariantsUniformSetter( (binding, buffer, props) -> buffer.set(0, props.getProperty(MaterialProperty.Color.BASE) ) );

        setDescriptorsetProviders( sceneDescriptorSet );

    }

}
