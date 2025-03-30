package org.sc.themis.renderer.material;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;

import java.util.HashMap;
import java.util.Map;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

public class MaterialMainVariant extends TObject {

  private final String identifier;
  private final MaterialRenderer materialRenderer;

  private final FrameKey<VkDescriptorSet> descriptorset = FrameKey.of(VkDescriptorSet.class);
  private final Map<Integer, FrameKey<VkBuffer>> buffers = new HashMap<>();

  public MaterialMainVariant(Configuration configuration, MaterialRenderer materialRenderer, String identifier) {
    super(configuration);
    this.materialRenderer = materialRenderer;
    this.identifier = identifier;
  }

  public MaterialRenderer getMaterial() {
    return this.materialRenderer;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void setup() throws ThemisException {
    setupDescriptorset();
    setupUniformDynamic();
  }

  public VkDescriptorSet getDescriptorSet(int frame) {
    return this.materialRenderer.getFrames().get(frame, this.descriptorset);
  }

  public int getAlignedOffset(int frame, int binding, int requestOffset) {
    VkBuffer buffer = this.materialRenderer.getFrames().get(frame, this.buffers.get(binding));
    return buffer.isAligned() ? requestOffset * buffer.getAlignedSize() : requestOffset;
  }

  public void setProperties(int offset, Material properties) throws ThemisException {

    for (Map.Entry<Integer, VkDescriptorSetBinding> bindingEntry :
        this.materialRenderer.getMainDescriptor().getBindings().entrySet()) {

      int bindingIdx = bindingEntry.getKey();
      VkDescriptorSetBinding binding = bindingEntry.getValue();

      if (binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC) {
        FrameKey<VkBuffer> bufferKey = this.buffers.get(bindingIdx);
        this.materialRenderer
            .getFrames()
            .update(
                bufferKey,
                (buffer) ->
                    this.materialRenderer
                        .getMainUniformSetter()
                        .set(bindingIdx, buffer, offset, properties));
      }
    }
  }

  public void cleanup() throws ThemisException {
    this.materialRenderer.getFrames().remove(this.descriptorset);
  }

  private void setupDescriptorset() throws ThemisException {
    VkDescriptorPool pool = this.materialRenderer.getMainDescriptorPool();
    this.materialRenderer.getFrames().create(this.descriptorset, pool::create);
  }

  private void setupUniformDynamic() throws ThemisException {

    for (Map.Entry<Integer, VkDescriptorSetBinding> bindingEntry :
        this.materialRenderer.getVariantsDescriptor().getBindings().entrySet()) {

      int bindingIdx = bindingEntry.getKey();
      VkDescriptorSetBinding binding = bindingEntry.getValue();
      VkBufferDescriptor bufferDescriptor =
          this.materialRenderer.getVariantsDescriptor().getBufferDescriptor(bindingIdx);

      if (binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER
          && bufferDescriptor != null) {
        FrameKey<VkBuffer> bufferKey = FrameKey.of(VkBuffer.class);
        this.buffers.put(bindingIdx, bufferKey);
        this.materialRenderer
            .getFrames()
            .create(
                bufferKey,
                () ->
                    new VkBuffer(
                        getConfiguration(),
                        this.materialRenderer.getDevice(),
                        this.materialRenderer.getAllocator(),
                        bufferDescriptor));
        this.materialRenderer
            .getFrames()
            .update(
                this.descriptorset,
                (frame, descriptorset) ->
                    descriptorset.bind(
                        bindingIdx, this.materialRenderer.getFrames().get(frame, bufferKey)));
      }
    }
  }
}
