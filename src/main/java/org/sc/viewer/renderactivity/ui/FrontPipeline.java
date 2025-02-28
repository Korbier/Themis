package org.sc.viewer.renderactivity.ui;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.pipeline.*;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.descriptorset.UIDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.shared.utils.MemorySizeUtils;
import org.sc.viewer.renderactivity.ViewerRendererActivity;

import static org.lwjgl.vulkan.VK10.*;

public class FrontPipeline extends TObject {

    private final String BACK_VERTEX_SRC = """
            #version 450
            
            layout(location = 0) out vec2 outTexture;
            layout(location = 1) out vec3 outColor;

            layout(location = 0) in vec2 position;
            layout(location = 1) in vec2 textureCoord;
            layout(location = 2) in vec3 color;
            
            layout(set = 0, binding = 0) uniform Global {
                mat4 projection;
                float fov;
                float znear;
                float zfar;
                vec2 resolution;
            } global;
            
            void main()
            {
                outTexture = textureCoord;
                outColor = color;
                gl_Position = global.projection * vec4(position, global.znear * -1, 1.0f);
            }
            """;
    private final String BACK_FRAGMENT_SRC = """ 
            #version 450
            
            layout(location = 0) in vec2 inTexture;
            layout(location = 1) in vec3 inColor;
            layout(location = 0) out vec4 outFragColor;
            
            layout(set = 0, binding = 1) uniform sampler2D textureSampler;
            
            void main() {
                outFragColor = vec4(inColor, 1.0f);// texture(textureSampler, inTexture);
            }
            """;

    private final VkDevice device;
    private final ViewerRendererActivity activity;
    private final VkRenderPass pass;

    private VkShaderProgram shaderProgram;
    private VkPipelineLayout pipelineLayout;
    private VkPipeline pipeline;

    private UIDescriptorSet uiDescriptorSet;

    public FrontPipeline(Configuration configuration, VkDevice device, ViewerRendererActivity activity, VkRenderPass pass) {
        super(configuration);
        this.device = device;
        this.activity = activity;
        this.pass = pass;
    }

    @Override
    public void setup() throws ThemisException {
        setupDescriptorset();
        setupPipeline();
    }

    private void setupDescriptorset() throws ThemisException {
        this.uiDescriptorSet = new UIDescriptorSet(getConfiguration(), this.activity.getRenderer());
        this.uiDescriptorSet.setup();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.pipeline.cleanup();
        this.pipelineLayout.cleanup();
        this.shaderProgram.cleanup();
        this.uiDescriptorSet.cleanup();
    }

    public VkPipeline getPipeline() {
        return this.pipeline;
    }

    public void updateAll(Scene scene) throws ThemisException {
        this.uiDescriptorSet.updateAll(scene);
    }

    public VkDescriptorSet getDescriptorset(int frame) {
        return this.uiDescriptorSet.getDescriptorSet(frame);
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

        this.shaderProgram = new VkShaderProgram(getConfiguration(), this.device, vertexShader, fragmentShader);
        this.shaderProgram.setup();

        this.pipelineLayout = new VkPipelineLayout(
            getConfiguration(), this.device,
            new VkPushConstantRange[0],
            this.uiDescriptorSet.getDescriptorSetLayout()
        );
        this.pipelineLayout.setup();

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkVertexInputStateDescriptor descriptor = new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX)
                    .attribute(VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F)  //2D position
                    .attribute(VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F)  //Texture
                    .attribute(VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F); //Color

            VkVertexInputState inputState = new VkVertexInputState(descriptor);
            inputState.setup(stack);

            this.pipeline = new VkPipeline(
                    getConfiguration(), this.device,
                    new VkPipelineDescriptor(this.pass, 0, true, 1, false, 1, 1, 1),
                    this.shaderProgram, this.pipelineLayout,
                    inputState
            );
            this.pipeline.setup();

        }

    }

}
