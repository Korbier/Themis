package org.sc.playground.font;

import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.sc.playground.shared.BaseRendererActivity;
import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.base.pipeline.*;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.base.resource.image.VkSampler;
import org.sc.themis.renderer.base.resource.image.VkSamplerDescriptor;
import org.sc.themis.renderer.base.sync.VkFence;
import org.sc.themis.renderer.resource.VkStagingImage;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.Image;
import org.sc.themis.shared.resource.font.Font;

public class FontRendererActivity extends BaseRendererActivity {

  private static final String SHADER_VERTEX_SOURCE =
      "src/main/resources/playground/font/vertex_shader.glsl";
  private static final String SHADER_VERTEX_COMPILED =
      "target/playground/font/vertex_shader.spirv";
  private static final String SHADER_FRAGMENT_SOURCE =
      "src/main/resources/playground/font/fragment_shader.glsl";
  private static final String SHADER_FRAGMENT_COMPILED =
      "target/playground/font/fragment_shader.spirv";

  /*** Pipeline ***/
  private VkShaderProgram shaderProgram;

  private VkPipelineLayout pipelineLayout;
  private VkPipeline pipeline;

  /*** Descriptorset ***/
  private static final FrameKey<VkDescriptorSet> FK_DESCRIPTORSET =
      FrameKey.of(VkDescriptorSet.class);

  private VkDescriptorSetLayout descriptorLayout;
  private VkDescriptorPool descriptorPool;

  private VkSampler sampler;
  private Image image;
  private VkStagingImage vkImage;

  public FontRendererActivity(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void render(Scene scene, long tpf) throws ThemisException {

    int frame = this.renderer.acquire(scene);

    VkCommand command = getCommand(frame);
    VkFence fence = getFence(frame);
    VkFrameBuffer framebuffer = getFramebuffer(frame);
    VkDescriptorSet descriptorSet = getFrames().get(frame, FK_DESCRIPTORSET);

    command.begin();
    command.beginRenderPass(this.renderPass, framebuffer);
    command.viewportAndScissor(this.renderer.getExtent());
    command.bindPipeline(this.pipeline);
    command.bindDescriptorSets(new int[0], descriptorSet);
    command.draw(6, 1, 0, 0);
    command.endRenderPass();
    command.end();
    command.submit(
        fence, this.renderer.getAcquireSemaphore(frame), this.renderer.getPresentSemaphore(frame));

    fence.waitForAndReset();
  }

  public void setupPipeline() throws ThemisException {
    this.setupImage();
    this.setupDescriptorSets();
    this.setupShaderProgram();
    this.setupPipelineAndLayout();
  }

  @Override
  public void cleanupPipeline() throws ThemisException {
    this.vkImage.cleanup();
    this.sampler.cleanup();
    this.descriptorPool.cleanup();
    this.descriptorLayout.cleanup();
    this.pipeline.cleanup();
    this.pipelineLayout.cleanup();
    this.shaderProgram.cleanup();
  }

  private void setupShaderProgram() throws ThemisException {

    try {

      VkShaderSourceCompiler.compileShaderIfChanged(
          SHADER_VERTEX_SOURCE, SHADER_VERTEX_COMPILED, Shaderc.shaderc_glsl_vertex_shader);
      VkShaderSourceCompiler.compileShaderIfChanged(
          SHADER_FRAGMENT_SOURCE, SHADER_FRAGMENT_COMPILED, Shaderc.shaderc_glsl_fragment_shader);

      VkShaderProgramStage vertexStage =
          new VkShaderProgramStage(
              VK_SHADER_STAGE_VERTEX_BIT, Files.readAllBytes(Paths.get(SHADER_VERTEX_COMPILED)));
      VkShaderProgramStage fragmentStage =
          new VkShaderProgramStage(
              VK_SHADER_STAGE_FRAGMENT_BIT,
              Files.readAllBytes(Paths.get(SHADER_FRAGMENT_COMPILED)));

      this.shaderProgram =
          new VkShaderProgram(getConfiguration(), renderer.getDevice(), vertexStage, fragmentStage);
      this.shaderProgram.setup();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void setupPipelineAndLayout() throws ThemisException {

    this.pipelineLayout =
        new VkPipelineLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            new VkPushConstantRange[0],
            this.descriptorLayout);
    this.pipelineLayout.setup();

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkVertexInputState inputState = new VkVertexInputState();
      inputState.setup(stack);

      this.pipeline =
          new VkPipeline(
              getConfiguration(),
              this.renderer.getDevice(),
              new VkPipelineDescriptor(this.renderPass, 0, false, 1, true, 1, 1, 1),
              this.shaderProgram,
              this.pipelineLayout,
              inputState);

      this.pipeline.setup();
    }
  }

  private void setupDescriptorSets() throws ThemisException {

    this.descriptorLayout =
        new VkDescriptorSetLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            VkDescriptorSetBinding.combinedImageSampler(0, VK_SHADER_STAGE_FRAGMENT_BIT));
    this.descriptorLayout.setup();

    this.descriptorPool =
        new VkDescriptorPool(
            getConfiguration(),
            this.renderer.getDevice(),
            this.renderer.getFrameCount(),
            this.descriptorLayout);
    this.descriptorPool.setup();

    getFrames()
        .create(
            FK_DESCRIPTORSET,
            () ->
                new VkDescriptorSet(
                    getConfiguration(),
                    this.renderer.getDevice(),
                    this.descriptorPool,
                    this.descriptorLayout));
    getFrames()
        .update(
            FK_DESCRIPTORSET,
            (descriptorset) -> descriptorset.bind(0, this.vkImage.getView(), this.sampler));
  }

  private void setupImage() throws ThemisException {

    this.sampler =
        new VkSampler(
            getConfiguration(),
            this.renderer.getDevice(),
            new VkSamplerDescriptor(VK_FILTER_LINEAR, 1, true));
    this.sampler.setup();

    Font font = Font.normal(18, Path.of("./src/main/resources/playground/font/CenturyGothic.ttf"));
    this.vkImage = this.renderer.getResourceAllocator().allocateImage(VK_FORMAT_R8G8B8A8_SRGB);
    this.vkImage.load(font.getTexture());

  }
}
