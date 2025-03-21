package org.sc.themis.scene.descriptorset;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.shared.utils.MemorySizeUtils;

/**
 * Descriptorset layout
 *
 * <p>MemorySizeUtils.VEC4F Selected instance identifier
 *
 * <p>Shader source (Write)
 *
 * <p>layout ( std140, set = X, binding = 1 ) buffer Storage { vec4 identifier; } selection;
 *
 * <p>Shader source (Read)
 *
 * <p>layout ( std140, set = X, binding = 0 ) readonly buffer Storage { vec4 identifier; }
 * selection;
 */
public class MousePickingDescriptorSet extends TObject {

  private static final FrameKey<VkBuffer> FK_BUFFER = FrameKey.of(VkBuffer.class);
  private static final FrameKey<VkDescriptorSet> FK_DESCRIPTORSET =
      FrameKey.of(VkDescriptorSet.class);

  private static final int BUFFER_SIZE = MemorySizeUtils.VEC4F; // Intance identifier
  private static final VkBufferDescriptor BUFFER_DESCRIPTOR =
      VkBufferDescriptor.descriptorsetStorageBuffer(BUFFER_SIZE);

  private final Renderer renderer;

  private VkDescriptorSetLayout descriptorSetLayout;
  private VkDescriptorPool descriptorPool;

  private final float[] identifier = new float[4];

  public MousePickingDescriptorSet(Configuration configuration, Renderer renderer) {
    super(configuration);
    this.renderer = renderer;
  }

  public VkDescriptorSetLayout getDescriptorSetLayout() {
    return this.descriptorSetLayout;
  }

  public VkDescriptorSet getDescriptorSet(int frame) {
    return this.renderer.getFramesInFlight().get(frame, FK_DESCRIPTORSET);
  }

  @Override
  public void setup() throws ThemisException {
    setupDescriptorLayout();
    setupDescriptorPool();
    setupBuffers();
    setupDescriptorSets();
  }

  public float[] getSelection(int frame) {
    VkBuffer buffer = this.renderer.getFramesInFlight().get(frame, FK_BUFFER);
    buffer.getMappedContent().rewind().asFloatBuffer().get(this.identifier);
    return this.identifier;
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
            (frame, descriptorset) ->
                descriptorset.bind(0, this.renderer.getFramesInFlight().get(frame, FK_BUFFER)));
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
            VkDescriptorSetBinding.storageBuffer(0, VK_SHADER_STAGE_FRAGMENT_BIT));
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
    this.renderer.getFramesInFlight().remove(FK_BUFFER);
    this.renderer.getFramesInFlight().remove(FK_DESCRIPTORSET);
    this.descriptorPool.cleanup();
    this.descriptorSetLayout.cleanup();
  }
}
