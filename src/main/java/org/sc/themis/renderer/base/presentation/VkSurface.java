package org.sc.themis.renderer.base.presentation;

import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.base.device.VkInstance;
import org.sc.themis.renderer.lang.VulkanObject;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.LogUtils;
import org.sc.themis.window.Window;
import org.slf4j.LoggerFactory;

public class VkSurface extends VulkanObject {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VkSurface.class);

  private final VkInstance instance;
  private final Window window;

  private long handle;

  public VkSurface(Configuration configuration, VkInstance instance, Window window) {
    super(configuration);
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
    vkSurface().destroyWindowSurface(this.instance.getHandle(), this.handle);
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
      vkSurface().createWindowSurface(this.instance.getHandle(), window.getHandle(), pSurface);
      this.handle = pSurface.get(0);
    }
  }

}
