package org.sc.viewer.renderactivity.shadow;

import org.sc.themis.renderer.base.sync.VkFence;
import org.sc.themis.renderer.base.sync.VkSemaphore;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.renderactivity.RenderPass;

public class ShadowRenderPass extends RenderPass {

  @Override
  public void setup() throws ThemisException {}

  @Override
  public void setup(Scene scene) throws ThemisException {}

  @Override
  public void cleanup() throws ThemisException {}

  @Override
  public void render(
      int frame, Scene scene, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore, VkFence fence)
      throws ThemisException {}

  @Override
  public void resize(Scene scene) throws ThemisException {}
}
