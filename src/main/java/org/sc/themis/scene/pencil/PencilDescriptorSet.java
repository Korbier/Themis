package org.sc.themis.scene.pencil;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetProvider;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.base.resource.image.VkSampler;
import org.sc.themis.renderer.base.resource.image.VkSamplerDescriptor;
import org.sc.themis.renderer.resource.VkStagingImage;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.old.STBFreeType;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Descriptorset layout.
 *
 * <pre>
 * MemorySizeUtils.MAT4x4F Projection
 * MemorySizeUtils.MAT4x4F View
 * MemorySizeUtils.VEC2F   Resolution</pre>
 *
 * <p>Shader source.
 *
 * <pre>
 * layout(std140, set = 0, binding = 0) uniform Global {
 *     mat4 projection;
 *     mat4 view;
 *     vec2 resolution;
 * } global;</pre>
 */
public class PencilDescriptorSet extends TObject implements VkDescriptorSetProvider {

  private static final FrameKey<VkBuffer> FK_BUFFER = FrameKey.of(VkBuffer.class);
  private static final FrameKey<VkDescriptorSet> FK_DESCRIPTORSET =
      FrameKey.of(VkDescriptorSet.class);
  private static final int BUFFER_SIZE =
      MemorySizeUtils.MAT4x4F + MemorySizeUtils.VEC3F + MemorySizeUtils.VEC2F; // resolution
  private static final VkBufferDescriptor BUFFER_DESCRIPTOR =
      new VkBufferDescriptor(
          BUFFER_SIZE, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);

  private final Renderer renderer;
  private final Pencil pencil;

  private VkDescriptorSetLayout descriptorSetLayout;
  private VkDescriptorPool descriptorPool;

  private VkSampler sampler;
  private VkStagingImage stgImage;

  /**
   * Constructor.
   *
   * @param configuration Globale configuration
   * @param renderer Renderer
   */
  public PencilDescriptorSet(Configuration configuration, Renderer renderer, Pencil pencil) {
    super(configuration);
    this.renderer = renderer;
    this.pencil = pencil;
  }

  /**
   * Update all framed data with provided scene.
   *
   * @param scene scene
   */
  public void updateAll(Scene scene) throws ThemisException {
    this.renderer.getFramesInFlight().update(FK_BUFFER, (frame, buffer) -> update(frame, scene));
  }

  /**
   * Update frame with provided scene.
   *
   * @param scene scene
   */
  public void update(int frame, Scene scene) {
    VkBuffer buffer = this.renderer.getFramesInFlight().get(frame, FK_BUFFER);
    buffer.set(0, scene.getProjection().orthographic());
    buffer.set(MemorySizeUtils.MAT4x4F, getConfiguration().scene().projection().fov());
    buffer.set(
        MemorySizeUtils.MAT4x4F + MemorySizeUtils.FLOAT,
        getConfiguration().scene().projection().znear());
    buffer.set(
        MemorySizeUtils.MAT4x4F + MemorySizeUtils.FLOAT * 2,
        getConfiguration().scene().projection().zfar());
    buffer.set(
        MemorySizeUtils.MAT4x4F + MemorySizeUtils.FLOAT * 3,
        this.renderer.getWindow().getResolution());
  }

  public VkDescriptorSetLayout getDescriptorSetLayout() {
    return this.descriptorSetLayout;
  }

  /**
   * Return descriptorset for provided frame.
   *
   * @param frame frame
   */
  public VkDescriptorSet getDescriptorSet(int frame) {
    return this.renderer.getFramesInFlight().get(frame, FK_DESCRIPTORSET);
  }

  @Override
  public void setup() throws ThemisException {
    setupDescriptorLayout();
    setupDescriptorPool();
    setupBuffers();
    setupImages();
    setupDescriptorSets();
  }

  private void setupImages() throws ThemisException {

    this.sampler =
        new VkSampler(
            getConfiguration(),
            this.renderer.getDevice(),
            new VkSamplerDescriptor(VK_FILTER_LINEAR, 1, true, false)
        );
    this.sampler.setup();

    this.stgImage = this.renderer.getResourceAllocator().allocateImage(VK_FORMAT_R8_UNORM);
    this.stgImage.load(STBFreeType.INSTANCE.getFontTexture());

  }

  private void setupDescriptorSets() throws ThemisException {
    this.renderer
        .getFramesInFlight()
        .create(
            FK_DESCRIPTORSET,
            () ->
                new VkDescriptorSet(
                    getConfiguration(),
                    this.renderer.getDevice(),
                    this.descriptorPool,
                    this.descriptorSetLayout));
    this.renderer
        .getFramesInFlight()
        .update(
            FK_DESCRIPTORSET,
            (frame, descriptorset) -> {
              descriptorset.bind(0, this.renderer.getFramesInFlight().get(frame, FK_BUFFER));
              descriptorset.bind(1, this.stgImage.getView(), this.sampler);
            });
  }

  private void setupBuffers() throws ThemisException {
    this.renderer
        .getFramesInFlight()
        .create(
            FK_BUFFER,
            () ->
                new VkBuffer(
                    getConfiguration(),
                    this.renderer.getDevice(),
                    this.renderer.getMemoryAllocator(),
                    BUFFER_DESCRIPTOR));
  }

  private void setupDescriptorLayout() throws ThemisException {
    this.descriptorSetLayout =
        new VkDescriptorSetLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            VkDescriptorSetBinding.uniform(0, VK_SHADER_STAGE_VERTEX_BIT),
            VkDescriptorSetBinding.combinedImageSampler(0, VK_SHADER_STAGE_FRAGMENT_BIT));
    this.descriptorSetLayout.setup();
  }

  private void setupDescriptorPool() throws ThemisException {
    this.descriptorPool =
        new VkDescriptorPool(
            getConfiguration(),
            this.renderer.getDevice(),
            this.renderer.getFramesInFlight().getSize(),
            this.descriptorSetLayout);
    this.descriptorPool.setup();
  }

  @Override
  public void cleanup() throws ThemisException {
    this.sampler.cleanup();
    this.renderer.getFramesInFlight().remove(FK_BUFFER);
    this.renderer.getFramesInFlight().remove(FK_DESCRIPTORSET);
    this.descriptorPool.cleanup();
    this.descriptorSetLayout.cleanup();
  }
}
