package org.sc.viewer.renderactivity.postprocess.postprocessor;

import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.base.pipeline.VkShaderSourceCompiler;
import org.sc.viewer.renderactivity.postprocess.PostProcessor;

public class ShowGridPostprocessor implements PostProcessor {

  public static final PostProcessor INSTANCE = new ShowGridPostprocessor();

  public static final String IDENTIFIER = "postprocessor.showGrid";

  private static final String VERTEX_SOURCE =
      """
         #version 450

         const vec3 positions[3] = vec3[3](
             vec3( -2.0f, 0.0f, 0.0f),
             vec3(  2.0f, 0.0f, 0.0f),
             vec3(  0.0f, 2.0f, 0.0f)
         );
        
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
  
         void main() {
             gl_Position = global.view * vec4(positions[gl_VertexIndex], 1.0f);
         }
         """;

  private static final String GEOMETRY_SOURCE =
      """
         #version 450
         
         const int lines_start = -4;
         const int lines_end = 4;
         const int lines_step = 2;

         const vec4 defaultColor = vec4(0.8f, 0.8f, 0.8f, 1.0f);
         const vec4 axeColor = vec4(0.8f, 0.0f, 0.0f, 1.0f );

         layout (triangles) in;
         layout (line_strip, max_vertices = 2 + 2 * ( (lines_end - lines_start) * lines_step + 1 ) * 2 ) out;

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

         void selectColor( float step ) {
             if ( step == 0 ) {
                 color = axeColor;
             } else {
                 color = defaultColor;
             }
         }

         void main() {

             for (float x = lines_start; x <= lines_end; x += (1.0f / lines_step) ){

                 selectColor( x );

                 gl_Position = global.projection * global.view * vec4(x, 0.0f, lines_start, 1.0f );
                 EmitVertex();
                 gl_Position = global.projection * global.view * vec4(x, 0.0f, lines_end, 1.0f );
                 EmitVertex();
                 EndPrimitive();

             }

             for ( float z = lines_start; z <= lines_end; z += (1.0f / lines_step) ){

                 selectColor( z );

                 gl_Position = global.projection * global.view * vec4(lines_start, 0.0f, z, 1.0f );
                 EmitVertex();
                 gl_Position = global.projection * global.view * vec4(lines_end, 0.0f, z, 1.0f );
                 EmitVertex();
                 EndPrimitive();

             }

             selectColor( 0 );

             gl_Position = global.projection * global.view * vec4(0.0f, lines_start, 0.0f, 1.0f );
             EmitVertex();
             gl_Position = global.projection * global.view * vec4(0.0f, lines_end, 0.0f, 1.0f );
             EmitVertex();
             EndPrimitive();

         }
         """;

  private static final String FRAGMENT_SOURCE =
      """
        #version 450
        
        layout(location = 0) out vec4 outFragColor;
        layout(location=0) in vec4 color;
        
        void main() {
            outFragColor = color;
        }
        """;

  @Override
  public String getIdentifier() {
    return IDENTIFIER;
  }

  @Override
  public DrawFrequency getFrequency() {
    return DrawFrequency.DRAW_ONCE;
  }

  @Override
  public byte[] getVertexShader() {
    return VkShaderSourceCompiler.compileShader(VERTEX_SOURCE, Shaderc.shaderc_glsl_vertex_shader);
  }

  @Override
  public byte[] getGeometryShader() {
    return VkShaderSourceCompiler.compileShader(GEOMETRY_SOURCE, Shaderc.shaderc_glsl_geometry_shader);
  }

  @Override
  public byte[] getFragmentShader() {
    return VkShaderSourceCompiler.compileShader(FRAGMENT_SOURCE, Shaderc.shaderc_glsl_fragment_shader);
  }
}
