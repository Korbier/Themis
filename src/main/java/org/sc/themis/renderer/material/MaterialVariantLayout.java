package org.sc.themis.renderer.material;

import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.base.resource.image.VkSamplerDescriptor;

import java.util.HashMap;
import java.util.Map;

public class MaterialVariantLayout {

  private final Map<Integer, VkDescriptorSetBinding> bindings = new HashMap<>();
  private final Map<Integer, VkBufferDescriptor> bufferDescriptors = new HashMap<>();
  private final Map<Integer, VkSamplerDescriptor> samplerBufferDescriptors = new HashMap<>();

  public static MaterialVariantLayout.Builder builder() {
    return new MaterialVariantLayout.Builder();
  }

  public void uniformDynamic(int binding, int shaderStage, VkBufferDescriptor bufferDescriptor) {
    this.bindings.put(binding, VkDescriptorSetBinding.dynamicUniform(binding, shaderStage));
    this.bufferDescriptors.put(binding, bufferDescriptor);
  }

  public void uniform(int binding, int shaderStage, VkBufferDescriptor bufferDescriptor) {
    this.bindings.put(binding, VkDescriptorSetBinding.uniform(binding, shaderStage));
    this.bufferDescriptors.put(binding, bufferDescriptor);
  }

  public void combinedImageSampler(int binding, int shaderStage, VkSamplerDescriptor samplerDescriptor) {
    this.bindings.put(binding, VkDescriptorSetBinding.combinedImageSampler(binding, shaderStage));
    this.samplerBufferDescriptors.put(binding, samplerDescriptor);
  }

  public Map<Integer, VkDescriptorSetBinding> bindings() {
    return this.bindings;
  }

  public VkBufferDescriptor getBufferDescriptor(int binding) {
    if (this.bufferDescriptors.containsKey(binding)) {
      return this.bufferDescriptors.get(binding);
    }
    return null;
  }

  public VkSamplerDescriptor getSamplerDescriptor(int binding) {

    if (this.samplerBufferDescriptors.containsKey(binding)) {
      return this.samplerBufferDescriptors.get(binding);
    }

    return null;

  }

  public static class Builder {

    private final MaterialVariantLayout layout;

    public Builder() {
      this.layout = new MaterialVariantLayout();
    }

    public MaterialVariantLayout.Builder uniformDynamic(int binding, int shaderStage, VkBufferDescriptor bufferDescriptor) {
      this.layout.uniformDynamic(binding, shaderStage, bufferDescriptor);
      return this;
    }

    public MaterialVariantLayout.Builder uniform(int binding, int shaderStage, VkBufferDescriptor bufferDescriptor) {
      this.layout.uniform(binding, shaderStage, bufferDescriptor);
      return this;
    }

    public MaterialVariantLayout.Builder combinedImageSampler(int binding, int shaderStage, VkSamplerDescriptor samplerDescriptor) {
      this.layout.combinedImageSampler(binding, shaderStage, samplerDescriptor);
      return this;
    }

    public MaterialVariantLayout build() {
      return this.layout;
    }

  }



}
