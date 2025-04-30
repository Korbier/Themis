package org.sc.themis.renderer.base.command.set;

import org.sc.themis.renderer.base.command.VkCommandBuffer;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;

public abstract class VkCommandSet extends Vulkan implements LifeCycle {

  private final VkCommandBuffer buffer;

  public VkCommandSet(VkCommandBuffer buffer) {
    this.buffer = buffer;
  }

  protected VkCommandBuffer buffer() {
    return this.buffer;
  }

  @Override
  public void setup() throws ThemisException {}

  @Override
  public void cleanup() throws ThemisException {}
}
