package org.sc.themis.renderer.material_old.setter;

import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.resource.image.VkSampler;
import org.sc.themis.shared.exception.ThemisException;

@FunctionalInterface
public interface CombinedImageSamplerSetter {
  void set(int binding, VkDescriptorSet descriptorset, VkSampler sampler, org.sc.themis.renderer.resource.material.Material meshProperties) throws ThemisException;
}
