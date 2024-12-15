package org.sc.playground.resource.stagingimage;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.playground.shared.BaseRendererActivity;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.pipeline.*;
import org.sc.themis.renderer.resource.staging.VkStagingImage;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.Image;
import org.sc.themis.shared.utils.LogUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.vulkan.VK10.*;

public class ResourceStagingImageRendererActivity extends BaseRendererActivity {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(ResourceStagingImageRendererActivity.class);

    private final static String SHADER_VERTEX_SOURCE = "src/main/resources/playground/resource/stagingimage/vertex_shader.glsl";
    private final static String SHADER_VERTEX_COMPILED = "target/playground/resource/stagingimage/vertex_shader.spirv";
    private final static String SHADER_FRAGMENT_SOURCE = "src/main/resources/playground/resource/stagingimage/fragment_shader.glsl";
    private final static String SHADER_FRAGMENT_COMPILED = "target/playground/resource/stagingimage/fragment_shader.spirv";

    /*** Pipeline ***/
    private VkShaderProgram shaderProgram;
    private VkPipelineLayout pipelineLayout;
    private VkPipeline pipeline;

    private VkStagingImage vkImage;

    public ResourceStagingImageRendererActivity(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void render(Scene scene, long tpf) throws ThemisException {

        int frame = this.renderer.acquire();

        VkCommand     command     = getCommand( frame );
        VkFence       fence       = getFence( frame );
        VkFrameBuffer framebuffer = getFramebuffer( frame );

        command.begin();
        command.beginRenderPass( this.renderPass, framebuffer );
        command.viewportAndScissor( this.renderer.getExtent() );
        command.bindPipeline( this.pipeline );
        command.draw( 3, 1, 0, 0);
        command.endRenderPass();
        command.end();
        command.submit( fence, this.renderer.getAcquireSemaphore( frame ), this.renderer.getPresentSemaphore( frame ) );

        fence.waitForAndReset();

    }

    public void setupPipeline() throws ThemisException {
        this.setupImage();
        this.setupShaderProgram();
        this.setupPipelineAndLayout();
    }

    @Override
    public void cleanupPipeline() throws ThemisException {
        this.vkImage.cleanup();
        this.pipeline.cleanup();
        this.pipelineLayout.cleanup();
        this.shaderProgram.cleanup();
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

        this.pipelineLayout = new VkPipelineLayout(getConfiguration(), this.renderer.getDevice(), new VkPushConstantRange[0] );
        this.pipelineLayout.setup();

        try (MemoryStack stack = MemoryStack.stackPush() ) {

            VkVertexInputState inputState = new VkVertexInputState();
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

    private void setupImage() throws ThemisException {

        /** Image **/
        Image image = Image.of("src/main/resources/playground/descriptorset/imagesampler/vulkan.png");
        this.vkImage = this.renderer.getResourceAllocator().allocateImage( VK_FORMAT_R8G8B8A8_SRGB );
        this.vkImage.load(image);

        LOG.infof("Image loaded (image view address = %s)", LogUtils.toHexString( this.vkImage.getView().getHandle() ) );

    }

}
