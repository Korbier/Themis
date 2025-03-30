package org.sc.themis.renderer.material;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

import java.util.HashMap;
import java.util.Map;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.base.resource.image.VkSampler;
import org.sc.themis.renderer.base.resource.image.VkSamplerDescriptor;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

public class MaterialVariant extends TObject {

  private final String identifier;
  private final MaterialRenderer materialRenderer;

  private final FrameKey<VkDescriptorSet> descriptorset = FrameKey.of(VkDescriptorSet.class);
  private final Map<Integer, FrameKey<VkBuffer>> buffers = new HashMap<>();
  private final Map<Integer, FrameKey<VkSampler>> samplers = new HashMap<>();

  public MaterialVariant(Configuration configuration, MaterialRenderer materialRenderer, String identifier) {
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
    setupUniform();
    setupCombinedImageSampler();
  }

  public VkDescriptorSet getDescriptorSet(int frame) {
    return this.materialRenderer.getFrames().get(frame, this.descriptorset);
  }

  public void setProperties(Material properties) throws ThemisException {

    for (Map.Entry<Integer, VkDescriptorSetBinding> bindingEntry :
        this.materialRenderer.getVariantsDescriptor().getBindings().entrySet()) {

      int bindingIdx = bindingEntry.getKey();
      VkDescriptorSetBinding binding = bindingEntry.getValue();

      if (binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER) {
        FrameKey<VkBuffer> bufferKey = this.buffers.get(bindingIdx);
        this.materialRenderer
            .getFrames()
            .update(
                bufferKey,
                (buffer) ->
                    this.materialRenderer.getVariantsUniformSetter().set(bindingIdx, buffer, properties));
      }

      if (binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER) {
        FrameKey<VkSampler> samplerKey = this.samplers.get(bindingIdx);
        this.materialRenderer
            .getFrames()
            .update(
                this.descriptorset,
                (frame, descriptorset) ->
                    this.materialRenderer
                        .getVariantsCombinedImageSamplerSetter()
                        .set(
                            bindingIdx,
                            descriptorset,
                            this.materialRenderer.getFrames().get(frame, samplerKey),
                            properties));
      }
    }
  }

  public void cleanup() throws ThemisException {
    this.materialRenderer.getFrames().remove(this.descriptorset);
  }

  private void setupDescriptorset() throws ThemisException {
    VkDescriptorPool pool = this.materialRenderer.getVariantsDescriptorPool();
    this.materialRenderer.getFrames().create(this.descriptorset, pool::create);
  }

  private void setupUniform() throws ThemisException {

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

  private void setupCombinedImageSampler() throws ThemisException {

    for (Map.Entry<Integer, VkDescriptorSetBinding> bindingEntry :
        this.materialRenderer.getVariantsDescriptor().getBindings().entrySet()) {

      int bindingIdx = bindingEntry.getKey();
      VkDescriptorSetBinding binding = bindingEntry.getValue();
      VkSamplerDescriptor samplerDescriptor =
          this.materialRenderer.getVariantsDescriptor().getSamplerDescriptor(bindingIdx);

      if (binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER
          && samplerDescriptor != null) {
        FrameKey<VkSampler> samplerKey = FrameKey.of(VkSampler.class);
        this.samplers.put(bindingIdx, samplerKey);
        this.materialRenderer
            .getFrames()
            .create(
                samplerKey,
                () ->
                    new VkSampler(
                        getConfiguration(), this.materialRenderer.getDevice(), samplerDescriptor));
      }
    }
  }
}
