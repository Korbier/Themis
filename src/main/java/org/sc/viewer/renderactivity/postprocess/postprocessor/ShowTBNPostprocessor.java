package org.sc.viewer.renderactivity.postprocess.postprocessor;

import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.base.pipeline.VkShaderSourceCompiler;
import org.sc.viewer.renderactivity.postprocess.PostProcessor;

public class ShowTBNPostprocessor implements PostProcessor {

  public static final PostProcessor INSTANCE = new ShowTBNPostprocessor();

  public static final String IDENTIFIER = "postprocessor.showTBN";

  private static final String VERTEX_SOURCE =
      """
            #version 450

            layout(location = 0) out vertex_out {
                vec3 normal;
                vec3 tangent;
                vec3 bitangent;
            } vs_out;

            layout(location = 0) in vec3 position;
            layout(location = 1) in vec3 normal;
            layout(location = 2) in vec2 texture;
            layout(location = 3) in vec3 tangent;
            layout(location = 4) in vec3 bitangent;

            layout(std140, set = 0, binding = 0) uniform Global {
                mat4 projection;
                mat4 view;
                mat4 projectionInv;
                mat4 viewInv;
                vec4 camera;
                vec2 resolution;
                uint utime;
            } global;

            layout(push_constant) uniform pushConstant {
                layout( offset = 0 ) mat4 matrix;
            } instance;

            vec3 _normalize( mat3 normalMatrix, vec3 toNormalize ) {
                return vec3(vec4(normalMatrix * normalize(toNormalize), 0.0));
            }

            void main() {

                gl_Position = global.view * instance.matrix * vec4(position, 1.0f);

                mat3 normalMatrix = mat3(transpose(inverse( global.view * instance.matrix ) ) );

                vs_out.normal    = _normalize( normalMatrix, normal );
                vs_out.tangent   = _normalize( normalMatrix, tangent );
                vs_out.bitangent = _normalize( normalMatrix, bitangent );

            }
            """;

  private static final String GEOMETRY_SOURCE =
      """
            #version 450

            layout (triangles) in;
            layout (line_strip, max_vertices = 18) out;

            layout(location = 0) in vertex_out {
                vec3 normal;
                vec3 tangent;
                vec3 bitangent;
            } geo_in[];

            layout(location=0) out vec4 color;

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

            const float MAGNITUDE = 0.2;

            void GenerateLine(vec3 v, int index)
            {
                gl_Position = global.projection * gl_in[index].gl_Position;
                EmitVertex();
                gl_Position = global.projection * (gl_in[index].gl_Position + vec4(v, 0.0) * MAGNITUDE);
                EmitVertex();
                EndPrimitive();
            }

            void main() {

                color = vec4(1.0f, 0.0f, 0.0f, 1.0f);
                GenerateLine(geo_in[0].normal, 0); // first vertex normal
                GenerateLine(geo_in[1].normal, 1); // second vertex normal
                GenerateLine(geo_in[2].normal, 2); // third vertex normal

                color = vec4(0.0f, 1.0f, 0.0f, 1.0f);
                GenerateLine(geo_in[0].tangent, 0); // first vertex tangent
                GenerateLine(geo_in[1].tangent, 1); // second vertex tangent
                GenerateLine(geo_in[2].tangent, 2); // third vertex tangent

                color = vec4(0.0f, 0.0f, 1.0f, 1.0f);
                GenerateLine(geo_in[0].bitangent, 0); // first vertex bitangent
                GenerateLine(geo_in[1].bitangent, 1); // second vertex bitangent
                GenerateLine(geo_in[2].bitangent, 2); // third vertex bitangent

            }
            """;

  private static final String FRAGMENT_SOURCE =
      """
            #version 450

            layout(location = 0) out vec4 outFragColor;

            layout(location=0) in vec4 color;

            layout(std140, set = 0, binding = 0) uniform Global {
                mat4 projection;
                mat4 view;
                mat4 projectionInv;
                mat4 viewInv;
                vec4 camera;
                vec2 resolution;
                uint utime;
            } global;

            void main() {
                outFragColor = color;
            }
            """;

  @Override
  public String getIdentifier() {
    return IDENTIFIER;
  }

  @Override
  public Frequency getFrequency() {
    return Frequency.PER_VERTEX;
  }

  @Override
  public byte[] getVertexShader() {
    return VkShaderSourceCompiler.compileShader(VERTEX_SOURCE, Shaderc.shaderc_glsl_vertex_shader);
  }

  @Override
  public byte[] getGeometryShader() {
    return VkShaderSourceCompiler.compileShader(
        GEOMETRY_SOURCE, Shaderc.shaderc_glsl_geometry_shader);
  }

  @Override
  public byte[] getFragmentShader() {
    return VkShaderSourceCompiler.compileShader(
        FRAGMENT_SOURCE, Shaderc.shaderc_glsl_fragment_shader);
  }
}
