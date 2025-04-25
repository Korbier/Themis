package org.sc.themis.renderer.base.presentation;

import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.base.device.VkInstance;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;
import org.sc.themis.shared.utils.LogUtils;
import org.sc.themis.window.Window;
import org.slf4j.LoggerFactory;

public class VkSurface extends Vulkan implements LifeCycle {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VkSurface.class);

  private final VkInstance instance;
  private final Window window;

  private long handle;

  public VkSurface(VkInstance instance, Window window) {
    this.instance = instance;
    this.window = window;
  }

  @Override
  public void setup() throws ThemisException {
    setupWindowSurface();
    logger.trace("Surface initialised ({})", this);
  }

  @Override
  public void cleanup() throws ThemisException {
   surface.destroyWindowSurface(this.instance.getHandle(), this.handle);
  }

  public long getHandle() {
    return this.handle;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{handle=" + LogUtils.toHexString(getHandle()) + "}";
  }

  private void setupWindowSurface() throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      LongBuffer pSurface = stack.mallocLong(1);
     surface.createWindowSurface(this.instance.getHandle(), window.getHandle(), pSurface);
      this.handle = pSurface.get(0);
    }
  }

}
