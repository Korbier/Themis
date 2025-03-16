package org.sc.themis.renderer.base.pipeline.descriptorset;

public interface VkDescriptorSetProvider {
  VkDescriptorSetLayout getDescriptorSetLayout();
  VkDescriptorSet getDescriptorSet(int frame);
}
