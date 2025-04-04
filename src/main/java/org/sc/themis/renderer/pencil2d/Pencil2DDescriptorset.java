package org.sc.themis.renderer.pencil2d;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.pipeline.descriptorset.*;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.base.resource.image.VkSampler;
import org.sc.themis.renderer.base.resource.image.VkSamplerDescriptor;
import org.sc.themis.renderer.base.resource.staging.VkStagingImage;
import org.sc.themis.renderer.resource.font.FontRepository;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.base.Projection;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class Pencil2DDescriptorset extends TObject implements VkDescriptorSetProvider {

  private static final FrameKey<VkBuffer> FK_BUFFER = FrameKey.of(VkBuffer.class);
  private static final FrameKey<VkDescriptorSet> FK_DESCRIPTORSET = FrameKey.of(VkDescriptorSet.class);
  private static final int BUFFER_SIZE = MemorySizeUtils.MAT4x4F + MemorySizeUtils.VEC3F + MemorySizeUtils.VEC2F; // resolution
  private static final VkBufferDescriptor BUFFER_DESCRIPTOR = new VkBufferDescriptor(BUFFER_SIZE, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);

  private final Renderer renderer;
  private final FontRepository fontRepository;

  private VkDescriptorSetLayout descriptorSetLayout;
  private VkDescriptorPool descriptorPool;

  private VkSampler sampler = null;
  private VkStagingImage stgImage = null;

  public Pencil2DDescriptorset(Configuration configuration, Renderer renderer, FontRepository fontRepository) {
    super(configuration);
    this.renderer = renderer;
    this.fontRepository= fontRepository;
  }

  @Override
  public VkDescriptorSetLayout getDescriptorSetLayout() {
    return this.descriptorSetLayout;
  }

  @Override
  public VkDescriptorSet getDescriptorSet(int frame) {
    return this.renderer.getFramesInFlight().get(frame, FK_DESCRIPTORSET);
  }

  @Override
  public void setup() throws ThemisException {
    setupDescriptorLayout();
    setupDescriptorPool();
    setupBuffers();
    setupFonts();
    setupDescriptorSets();
  }

  @Override
  public void cleanup() throws ThemisException {
    if (this.sampler != null) {
      this.sampler.cleanup();
    }
    this.renderer.getFramesInFlight().remove(FK_BUFFER);
    this.renderer.getFramesInFlight().remove(FK_DESCRIPTORSET);
    this.descriptorPool.cleanup();
    this.descriptorSetLayout.cleanup();
  }

  public void updateAll(Scene scene) throws ThemisException {
    this.renderer.getFramesInFlight().update(FK_BUFFER, (frame, _) -> update(frame, scene));
  }

  public void update(int frame, Scene scene) {

    Projection projection = scene.getProjection();

    VkBuffer buffer = this.renderer.getFramesInFlight().get(frame, FK_BUFFER);
    buffer.set(0, projection.orthographic());
    buffer.set(MemorySizeUtils.MAT4x4F, projection.fov());
    buffer.set(MemorySizeUtils.MAT4x4F + MemorySizeUtils.FLOAT, projection.znear());
    buffer.set(MemorySizeUtils.MAT4x4F + MemorySizeUtils.FLOAT * 2, projection.zfar());
    buffer.set(MemorySizeUtils.MAT4x4F + MemorySizeUtils.FLOAT * 3, this.renderer.getWindow().getResolution());

  }

  private void setupDescriptorLayout() throws ThemisException {
    this.descriptorSetLayout = new VkDescriptorSetLayout(
        getConfiguration(), this.renderer.getDevice(),
        VkDescriptorSetBinding.uniform(0, VK_SHADER_STAGE_VERTEX_BIT),
        VkDescriptorSetBinding.combinedImageSampler(0, VK_SHADER_STAGE_FRAGMENT_BIT)
    );
    this.descriptorSetLayout.setup();
  }

  private void setupDescriptorPool() throws ThemisException {
    this.descriptorPool = new VkDescriptorPool(
        getConfiguration(), this.renderer.getDevice(),
        this.renderer.getFramesInFlight().getSize(),
        this.descriptorSetLayout
    );
    this.descriptorPool.setup();
  }

  private void setupBuffers() throws ThemisException {
    this.renderer.getFramesInFlight().create( FK_BUFFER, () ->
        new VkBuffer( getConfiguration(), this.renderer.getDevice(), this.renderer.getMemoryAllocator(), BUFFER_DESCRIPTOR)
    );
  }

  private void setupFonts() throws ThemisException {

    if (isFontRepositoryAvailable()) {

      this.sampler = new VkSampler(getConfiguration(), this.renderer.getDevice(), new VkSamplerDescriptor(VK_FILTER_LINEAR, 1, true, false));
      this.sampler.setup();

      this.stgImage = this.renderer.getResourceAllocator().allocateImage(VK_FORMAT_R8_UNORM, this.fontRepository.size());
      this.stgImage.load(this.fontRepository.getTextures());

    }

  }

  private void setupDescriptorSets() throws ThemisException {
    this.renderer.getFramesInFlight().create(FK_DESCRIPTORSET, () ->
        new VkDescriptorSet( getConfiguration(), this.renderer.getDevice(), this.descriptorPool, this.descriptorSetLayout)
    );
    this.renderer.getFramesInFlight().update( FK_DESCRIPTORSET, (frame, descriptorset) -> {
      descriptorset.bind(0, this.renderer.getFramesInFlight().get(frame, FK_BUFFER));
      if (isFontRepositoryAvailable() ) descriptorset.bind(1, this.stgImage.getView(), this.sampler);
    });
  }

  private boolean isFontRepositoryAvailable() {
    return this.fontRepository != null && this.fontRepository.size() > 0;
  }


}
