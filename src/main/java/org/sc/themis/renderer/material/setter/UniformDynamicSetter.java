package org.sc.themis.renderer.material.setter;

import org.sc.themis.renderer.base.resource.buffer.VkBuffer;

@FunctionalInterface
public interface UniformDynamicSetter {
  void set(int binding, VkBuffer buffer, int offset, org.sc.themis.renderer.resource.material.Material properties);
}