package org.sc.playground.texturearray;

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
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.Image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.vulkan.VK10.*;

public class TextureArrayRendererActivity extends BaseRendererActivity {

  private static final String SHADER_VERTEX_SOURCE =
      "src/main/resources/playground/texturearray/vertex_shader.glsl";
  private static final String SHADER_VERTEX_COMPILED =
      "target/playground/texturearray/vertex_shader.spirv";
  private static final String SHADER_FRAGMENT_SOURCE =
      "src/main/resources/playground/texturearray/fragment_shader.glsl";
  private static final String SHADER_FRAGMENT_COMPILED =
      "target/playground/texturearray/fragment_shader.spirv";

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
  private Image imageA;
  private Image imageB;
  private VkStagingImage vkImage;

  public TextureArrayRendererActivity(Configuration configuration) {
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
              new VkPipelineDescriptor(this.renderPass, 0, false, 1, false, 1, 1, 1),
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

    this.imageA = Image.of("src/main/resources/playground/texturearray/mars.jpg");
    this.imageB = Image.of("src/main/resources/playground/texturearray/mercure.jpg");

    this.vkImage = this.renderer.getResourceAllocator().allocateImage(VK_FORMAT_R8G8B8A8_SRGB, 2);
    this.vkImage.load(this.imageA, this.imageB);
  }
}
