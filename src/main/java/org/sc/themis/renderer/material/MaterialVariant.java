package org.sc.themis.renderer.material;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.base.resource.image.VkSampler;
import org.sc.themis.renderer.base.resource.image.VkSamplerDescriptor;
import org.sc.themis.shared.exception.ThemisException;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

public class MaterialVariant<O> {

  private final MaterialVariantLayout layout;
  private final VkDescriptorPool pool;
  private final MaterialVariantSetter<O> consumer;

  private final FrameKey<VkDescriptorSet> descriptorset = FrameKey.of(VkDescriptorSet.class);
  private final Map<Integer, FrameKey<VkBuffer>> buffers = new HashMap<>();
  private final Map<Integer, FrameKey<VkSampler>> samplers = new HashMap<>();

  public MaterialVariant(MaterialVariantLayout layout, VkDescriptorPool pool, MaterialVariantSetter<O> consumer) {
    this.layout = layout;
    this.pool = pool;
    this.consumer = consumer;
  }

  public void setup(Renderer renderer) throws ThemisException {
    setupDescriptorset(renderer);
    setupUnderlyingBuffers(renderer);
  }

  public void cleanup(Renderer renderer) throws ThemisException {
    renderer.getFramesInFlight().remove(this.descriptorset);
  }

  public FrameKey<VkDescriptorSet> getDescriptorKey() {
    return this.descriptorset;
  }

  public FrameKey<VkBuffer> getBufferKey(int bindingIdx) {
    return this.buffers.get(bindingIdx);
  }

  public FrameKey<VkSampler> getSamplerKey(int bindingIdx) {
    return this.samplers.get(bindingIdx);
  }

  public void set(Renderer renderer, O value) throws ThemisException {
    this.consumer.set(renderer, this, value);
  }

  private void setupDescriptorset(Renderer renderer) throws ThemisException {
    renderer.getFramesInFlight().create(this.descriptorset, pool::create);
  }

  private void setupUnderlyingBuffers(Renderer renderer) throws ThemisException {

    for (Map.Entry<Integer, VkDescriptorSetBinding> entry : this.layout.bindings().entrySet()) {

      int bindingIdx = entry.getKey();
      VkDescriptorSetBinding binding = entry.getValue();

      switch (binding.getDescriptorType()) {
        case VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER -> setupBuffer(renderer, bindingIdx, this.layout.getBufferDescriptor(bindingIdx));
        case VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER -> setupCombinedImageSampler(renderer, bindingIdx, this.layout.getSamplerDescriptor(bindingIdx));
        default -> throw new ThemisException("Unsupported descriptor type: " + binding.getDescriptorType());
      }

    }

  }

  private void setupBuffer(Renderer renderer, int bindingIdx,VkBufferDescriptor bufferDescriptor) throws ThemisException {
    FrameKey<VkBuffer> bufferKey = FrameKey.of(VkBuffer.class);
    this.buffers.put(bindingIdx, bufferKey);
    renderer.getFramesInFlight().create(bufferKey, () -> new VkBuffer(renderer.getDevice(), renderer.getMemoryAllocator(), bufferDescriptor));
    renderer.getFramesInFlight().update(this.descriptorset, (frame, descriptorset) -> descriptorset.bind(bindingIdx, renderer.getFramesInFlight().get(frame, bufferKey)));
  }

  private void setupCombinedImageSampler(Renderer renderer, int bindingIdx, VkSamplerDescriptor samplerDescriptor) throws ThemisException {
    FrameKey<VkSampler> samplerKey = FrameKey.of(VkSampler.class);
    this.samplers.put(bindingIdx, samplerKey);
    renderer.getFramesInFlight().create(samplerKey, () -> new VkSampler(renderer.getDevice(), samplerDescriptor));
  }

}
