package org.sc.playground.scene.cube2;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.playground.shared.BaseRendererActivity;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.pipeline.*;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.scene.*;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public class SceneCube2RendererActivity extends BaseRendererActivity {

    private final static String SHADER_VERTEX_SOURCE = "src/main/resources/playground/scene/cube2/vertex_shader.glsl";
    private final static String SHADER_VERTEX_COMPILED = "target/playground/scene/cube2/vertex_shader.spirv";
    private final static String SHADER_FRAGMENT_SOURCE = "src/main/resources/playground/scene/cube2/fragment_shader.glsl";
    private final static String SHADER_FRAGMENT_COMPILED = "target/playground/scene/cube2/fragment_shader.spirv";

    private VkShaderProgram shaderProgram;
    private VkPipelineLayout pipelineLayout;
    private VkPipeline pipeline;

    private SceneDescriptorSet sceneDescriptorSet;
    private ColorMaterial baseColorDescriptorSet;

    public SceneCube2RendererActivity(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void render(Scene scene, long tpf) throws ThemisException {

        int frame = this.renderer.acquire(scene);

        this.sceneDescriptorSet.update( frame, scene );

        VkCommand       command     = getCommand( frame );
        VkFence         fence       = getFence( frame );
        VkFrameBuffer   framebuffer = getFramebuffer( frame );
        VkDescriptorSet sceneDescriptorSet = this.sceneDescriptorSet.getDescriptorSet( frame );

        command.begin();
        command.beginRenderPass( this.renderPass, framebuffer );
        command.viewportAndScissor( this.renderer.getExtent() );
        command.bindPipeline(this.pipeline);

        for ( Model model : scene.getModels() ) {
            if ( model.isRenderable() ) {
                for (Mesh mesh : model.getMeshes() ) {

                    VkDescriptorSet materialDescriptorSet = this.baseColorDescriptorSet.getDescriptorSet( mesh.getMaterial(), frame );

                    command.bindDescriptorSets( new int[0], sceneDescriptorSet, materialDescriptorSet );
                    command.bindBuffers(mesh.getVerticesBuffer(), mesh.getIndicesBuffer());

                    for (Instance instance : model.getInstances() ) {
                        command.pushConstant( VK_SHADER_STAGE_VERTEX_BIT, 0, instance.matrix() );
                        command.drawIndexed(mesh.getIndiceCount());
                    }

                }
            }
        }

        command.endRenderPass();
        command.end();
        command.submit( fence, this.renderer.getAcquireSemaphore( frame ), this.renderer.getPresentSemaphore( frame ) );

        fence.waitForAndReset();

    }

    @Override
    public void setup( Scene scene ) throws ThemisException {

        List<Material> materials = new ArrayList<>();

        for ( Model model : scene.getModels() ) {
            materials.addAll(Arrays.asList(model.getMaterials()));
        }

        this.baseColorDescriptorSet.setMaterial( materials.toArray( new Material[0] ) );

    }

    @Override
    public void setupPipeline() throws ThemisException {
        this.setupSceneDescriptorSet();
        this.setupShaderProgram();
        this.setupPipelineAndLayout();
    }

    @Override
    public void cleanupPipeline() throws ThemisException {
        this.pipeline.cleanup();
        this.pipelineLayout.cleanup();
        this.shaderProgram.cleanup();
        this.baseColorDescriptorSet.cleanup();
        this.sceneDescriptorSet.cleanup();
    }

    private void setupSceneDescriptorSet() throws ThemisException {

        this.sceneDescriptorSet = new SceneDescriptorSet( getConfiguration(), this.renderer);
        this.sceneDescriptorSet.setup();

        this.baseColorDescriptorSet = new ColorMaterial(getConfiguration(), this.renderer );
        this.baseColorDescriptorSet.setup();

    }

    private void setupShaderProgram() throws ThemisException {

        try {

            VkShaderSourceCompiler.compileShaderIfChanged(SHADER_VERTEX_SOURCE, SHADER_VERTEX_COMPILED, Shaderc.shaderc_glsl_vertex_shader);
            VkShaderSourceCompiler.compileShaderIfChanged(SHADER_FRAGMENT_SOURCE, SHADER_FRAGMENT_COMPILED, Shaderc.shaderc_glsl_fragment_shader);

            VkShaderProgramStage vertexStage = new VkShaderProgramStage(VK_SHADER_STAGE_VERTEX_BIT, Files.readAllBytes(Paths.get(SHADER_VERTEX_COMPILED)));
            VkShaderProgramStage fragmentStage = new VkShaderProgramStage(VK_SHADER_STAGE_FRAGMENT_BIT, Files.readAllBytes(Paths.get(SHADER_FRAGMENT_COMPILED)));

            this.shaderProgram = new VkShaderProgram(getConfiguration(), renderer.getDevice(), vertexStage, fragmentStage);
            this.shaderProgram.setup();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void setupPipelineAndLayout() throws ThemisException {

        this.pipelineLayout = new VkPipelineLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            new VkPushConstantRange[] {
                new VkPushConstantRange( VK_SHADER_STAGE_VERTEX_BIT, 0, MemorySizeUtils.MAT4x4F ),
            },
            this.sceneDescriptorSet.getDescriptorSetLayout(),
            this.baseColorDescriptorSet.getDescriptorSetLayout()
        );
        this.pipelineLayout.setup();

        try (MemoryStack stack = MemoryStack.stackPush() ) {

            VkVertexInputStateDescriptor descriptor1 = new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX)
                    .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Position
                    .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Normal
                    .attribute( VK_FORMAT_R32G32_SFLOAT, MemorySizeUtils.VEC2F ) //Texture
                    .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ) //Tangent
                    .attribute( VK_FORMAT_R32G32B32_SFLOAT, MemorySizeUtils.VEC3F ); //Bitangent

            VkVertexInputState inputState = new VkVertexInputState(descriptor1);
            inputState.setup( stack );

            this.pipeline = new VkPipeline(
                getConfiguration(),
                this.renderer.getDevice(),
                new VkPipelineDescriptor(this.renderPass, 0, false, 1, false, 1, 1, 1),
                this.shaderProgram,
                this.pipelineLayout,
                inputState
            );

            this.pipeline.setup();

        }

    }


}
