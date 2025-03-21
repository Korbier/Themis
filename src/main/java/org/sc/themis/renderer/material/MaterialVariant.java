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
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

public class MaterialVariant extends TObject {

  private final String identifier;
  private final Material material;

  private final FrameKey<VkDescriptorSet> descriptorset = FrameKey.of(VkDescriptorSet.class);
  private final Map<Integer, FrameKey<VkBuffer>> buffers = new HashMap<>();
  private final Map<Integer, FrameKey<VkSampler>> samplers = new HashMap<>();

  public MaterialVariant(Configuration configuration, Material material, String identifier) {
    super(configuration);
    this.material = material;
    this.identifier = identifier;
  }

  public Material getMaterial() {
    return this.material;
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
    return this.material.getFrames().get(frame, this.descriptorset);
  }

  public void setProperties(MaterialProperties properties) throws ThemisException {

    for (Map.Entry<Integer, VkDescriptorSetBinding> bindingEntry :
        this.material.getVariantsDescriptor().getBindings().entrySet()) {

      int bindingIdx = bindingEntry.getKey();
      VkDescriptorSetBinding binding = bindingEntry.getValue();

      if (binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER) {
        FrameKey<VkBuffer> bufferKey = this.buffers.get(bindingIdx);
        this.material
            .getFrames()
            .update(
                bufferKey,
                (buffer) ->
                    this.material.getVariantsUniformSetter().set(bindingIdx, buffer, properties));
      }

      if (binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER) {
        FrameKey<VkSampler> samplerKey = this.samplers.get(bindingIdx);
        this.material
            .getFrames()
            .update(
                this.descriptorset,
                (frame, descriptorset) ->
                    this.material
                        .getVariantsCombinedImageSamplerSetter()
                        .set(
                            bindingIdx,
                            descriptorset,
                            this.material.getFrames().get(frame, samplerKey),
                            properties));
      }
    }
  }

  public void cleanup() throws ThemisException {
    this.material.getFrames().remove(this.descriptorset);
  }

  private void setupDescriptorset() throws ThemisException {
    VkDescriptorPool pool = this.material.getVariantsDescriptorPool();
    this.material.getFrames().create(this.descriptorset, pool::create);
  }

  private void setupUniform() throws ThemisException {

    for (Map.Entry<Integer, VkDescriptorSetBinding> bindingEntry :
        this.material.getVariantsDescriptor().getBindings().entrySet()) {

      int bindingIdx = bindingEntry.getKey();
      VkDescriptorSetBinding binding = bindingEntry.getValue();
      VkBufferDescriptor bufferDescriptor =
          this.material.getVariantsDescriptor().getBufferDescriptor(bindingIdx);

      if (binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER
          && bufferDescriptor != null) {
        FrameKey<VkBuffer> bufferKey = FrameKey.of(VkBuffer.class);
        this.buffers.put(bindingIdx, bufferKey);
        this.material
            .getFrames()
            .create(
                bufferKey,
                () ->
                    new VkBuffer(
                        getConfiguration(),
                        this.material.getDevice(),
                        this.material.getAllocator(),
                        bufferDescriptor));
        this.material
            .getFrames()
            .update(
                this.descriptorset,
                (frame, descriptorset) ->
                    descriptorset.bind(
                        bindingIdx, this.material.getFrames().get(frame, bufferKey)));
      }
    }
  }

  private void setupCombinedImageSampler() throws ThemisException {

    for (Map.Entry<Integer, VkDescriptorSetBinding> bindingEntry :
        this.material.getVariantsDescriptor().getBindings().entrySet()) {

      int bindingIdx = bindingEntry.getKey();
      VkDescriptorSetBinding binding = bindingEntry.getValue();
      VkSamplerDescriptor samplerDescriptor =
          this.material.getVariantsDescriptor().getSamplerDescriptor(bindingIdx);

      if (binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER
          && samplerDescriptor != null) {
        FrameKey<VkSampler> samplerKey = FrameKey.of(VkSampler.class);
        this.samplers.put(bindingIdx, samplerKey);
        this.material
            .getFrames()
            .create(
                samplerKey,
                () ->
                    new VkSampler(
                        getConfiguration(), this.material.getDevice(), samplerDescriptor));
      }
    }
  }
}
