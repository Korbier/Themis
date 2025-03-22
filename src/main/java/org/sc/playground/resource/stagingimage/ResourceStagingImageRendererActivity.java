package org.sc.playground.resource.stagingimage;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.playground.shared.BaseRendererActivity;
import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.base.pipeline.VkPipeline;
import org.sc.themis.renderer.base.pipeline.VkPipelineDescriptor;
import org.sc.themis.renderer.base.pipeline.VkPipelineLayout;
import org.sc.themis.renderer.base.pipeline.VkPushConstantRange;
import org.sc.themis.renderer.base.pipeline.VkShaderProgram;
import org.sc.themis.renderer.base.pipeline.VkShaderProgramStage;
import org.sc.themis.renderer.base.pipeline.VkShaderSourceCompiler;
import org.sc.themis.renderer.base.pipeline.VkVertexInputState;
import org.sc.themis.renderer.base.sync.VkFence;
import org.sc.themis.renderer.resource.VkStagingImage;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.Image;
import org.sc.themis.shared.resource.loader.descriptor.TextureResourceDescriptor;
import org.sc.themis.shared.resource.loader.ResourceEnum;
import org.sc.themis.shared.resource.ResourceLoader;
import org.sc.themis.shared.utils.LogUtils;
import org.slf4j.LoggerFactory;

public class ResourceStagingImageRendererActivity extends BaseRendererActivity {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ResourceStagingImageRendererActivity.class);

  private static final String SHADER_VERTEX_SOURCE = "src/main/resources/playground/resource/stagingimage/vertex_shader.glsl";
  private static final String SHADER_VERTEX_COMPILED = "target/playground/resource/stagingimage/vertex_shader.spirv";
  private static final String SHADER_FRAGMENT_SOURCE = "src/main/resources/playground/resource/stagingimage/fragment_shader.glsl";
  private static final String SHADER_FRAGMENT_COMPILED = "target/playground/resource/stagingimage/fragment_shader.spirv";

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

    int frame = this.renderer.acquire(scene);

    VkCommand command = getCommand(frame);
    VkFence fence = getFence(frame);
    VkFrameBuffer framebuffer = getFramebuffer(frame);

    command.begin();
    command.beginRenderPass(this.renderPass, framebuffer);
    command.viewportAndScissor(this.renderer.getExtent());
    command.bindPipeline(this.pipeline);
    command.draw(3, 1, 0, 0);
    command.endRenderPass();
    command.end();
    command.submit(fence, this.renderer.getAcquireSemaphore(frame), this.renderer.getPresentSemaphore(frame));

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

    this.pipelineLayout = new VkPipelineLayout(getConfiguration(), this.renderer.getDevice(), new VkPushConstantRange[0]);
    this.pipelineLayout.setup();

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkVertexInputState inputState = new VkVertexInputState();
      inputState.setup(stack);

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

    Image image = ResourceLoader.get().get(ResourceEnum.TEXTURE, TextureResourceDescriptor.of("vulkan.png"));
    this.vkImage = this.renderer.getResourceAllocator().allocateImage(VK_FORMAT_R8G8B8A8_SRGB);
    this.vkImage.load(image);

    logger.info("Image loaded (image view address = {})",LogUtils.toHexString(this.vkImage.getView().getHandle()));

  }
}
