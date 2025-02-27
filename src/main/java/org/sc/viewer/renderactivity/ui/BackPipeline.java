package org.sc.viewer.renderactivity.ui;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.pipeline.*;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.viewer.renderactivity.ViewerRendererActivity;


public class BackPipeline extends TObject {

    //Back pipeline
    private final String BACK_VERTEX_SRC = """
            #version 450
            
            layout (location = 0) out vec2 outTextCoord;

            void main()
            {
                outTextCoord = vec2((gl_VertexIndex << 1) & 2, gl_VertexIndex & 2);
                gl_Position  =  vec4(outTextCoord.x * 2.0f - 1.0f, outTextCoord.y * -2.0f + 1.0f, 0.0f, 1.0f);
            }
            """;
    private final String BACK_FRAGMENT_SRC = """ 
            #version 450
            
            layout(location = 0) in  vec2 inTextureCoords;
            layout(location = 0) out vec4 outFragColor;
            
            layout(set = 0, binding = 0) uniform sampler2D depthSampler;
            layout(set = 0, binding = 1) uniform sampler2D textureSampler;
            
            void main() {
                outFragColor = texture(textureSampler, inTextureCoords);
            }
            """;

    private final VkDevice device;
    private final ViewerRendererActivity activity;
    private final VkRenderPass pass;

    private VkShaderProgram backShaderProgram;
    private VkPipelineLayout backPipelineLayout;
    private VkPipeline backPipeline;

    public BackPipeline(Configuration configuration, VkDevice device, ViewerRendererActivity activity, VkRenderPass pass) {
        super(configuration);
        this.device = device;
        this.activity = activity;
        this.pass = pass;
    }

    @Override
    public void setup() throws ThemisException {
        setupPipeline();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.backPipeline.cleanup();
        this.backPipelineLayout.cleanup();
        this.backShaderProgram.cleanup();
    }

    public VkPipeline getPipeline() {
        return this.backPipeline;
    }

    private void setupPipeline() throws ThemisException {

        VkShaderProgramStage vertexShader = new VkShaderProgramStage(
                VK_SHADER_STAGE_VERTEX_BIT,
                VkShaderSourceCompiler.compileShader(BACK_VERTEX_SRC, Shaderc.shaderc_glsl_vertex_shader)
        );

        VkShaderProgramStage fragmentShader = new VkShaderProgramStage(
                VK_SHADER_STAGE_FRAGMENT_BIT,
                VkShaderSourceCompiler.compileShader(BACK_FRAGMENT_SRC, Shaderc.shaderc_glsl_fragment_shader)
        );

        this.backShaderProgram = new VkShaderProgram( getConfiguration(), this.device, vertexShader, fragmentShader );
        this.backShaderProgram.setup();

        this.backPipelineLayout = new VkPipelineLayout(
                getConfiguration(), this.device,
                new VkPushConstantRange[0],
                this.activity.getGeometryDescriptorset().getDescriptorSetLayout()
        );
        this.backPipelineLayout.setup();

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkVertexInputState backInputState = new VkVertexInputState();
            backInputState.setup(stack);

            this.backPipeline = new VkPipeline(
                    getConfiguration(), this.device,
                    new VkPipelineDescriptor(this.pass, 0, false, 1, false, 1, 1, 1),
                    this.backShaderProgram, this.backPipelineLayout,
                    backInputState
            );
            this.backPipeline.setup();

        }

    }

}
