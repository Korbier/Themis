package org.sc.themis.renderer.base.queue;

import org.sc.themis.core.LifeCycle;
import org.sc.themis.shared.exception.ThemisException;

import java.util.Objects;

public class VkQueue implements LifeCycle {

  private final org.lwjgl.vulkan.VkQueue vkQueue;
  private final int queueFamilyIndex;

  public VkQueue(org.lwjgl.vulkan.VkQueue queue, int queueFamilyIndex) {
    this.vkQueue = queue;
    this.queueFamilyIndex = queueFamilyIndex;
  }

  public void setup() throws ThemisException {
  }

  @Override
  public void cleanup() throws ThemisException {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VkQueue vkQueue1 = (VkQueue) o;
    return Objects.equals(vkQueue.address(), vkQueue1.vkQueue.address());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(vkQueue.address());
  }

  public org.lwjgl.vulkan.VkQueue getHandle() {
    return this.vkQueue;
  }

  public int getQueueFamilyIndex() {
    return this.queueFamilyIndex;
  }

}
