package org.sc.themis.renderer.material_old.setter;

import org.sc.themis.renderer.base.resource.buffer.VkBuffer;

@FunctionalInterface
public interface UniformSetter {
  void set(int binding, VkBuffer buffer, org.sc.themis.renderer.resource.material.Material properties);
}