package org.sc.viewer.renderactivity;

import org.lwjgl.vulkan.VkExtent2D;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.base.framebuffer.VkFrameBufferAttachments;
import org.sc.themis.renderer.base.resource.image.VkImageView;
import org.sc.themis.renderer.base.sync.VkFence;
import org.sc.themis.renderer.base.sync.VkSemaphore;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

public abstract class RenderPass extends TObject {

  private ViewerRendererActivity viewerActivity;

  public RenderPass(Configuration configuration) {
    super(configuration);
  }

  protected Renderer getRenderer() {
    return this.viewerActivity.getRenderer();
  }

  protected VkDevice getDevice() {
    return this.viewerActivity.getDevice();
  }

  protected VkExtent2D getExtent2D() {
    return this.viewerActivity.getRenderer().getExtent();
  }

  protected int getImageFormat() {
    return this.viewerActivity.getRenderer().getImageFormat();
  }

  protected Frames getFrames() {
    return this.viewerActivity.getFrames();
  }

  protected VkImageView getImageView(int frame) {
    return this.viewerActivity.getRenderer().getImageView(frame);
  }

  protected VkFrameBufferAttachments getGeometryFrameBufferAttachments() {
    return getViewerActivity().getGeometryFrameBufferAttachments();
  }

  public final void setup(ViewerRendererActivity activity) throws ThemisException {
    this.viewerActivity = activity;
    this.setup();
  }

  public ViewerRendererActivity getViewerActivity() {
    return this.viewerActivity;
  }

  public void render(int frame, Scene scene, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore)
      throws ThemisException {
    render(frame, scene, waitSemaphore, signalSemaphore, null);
  }

  public abstract void setup(Scene scene) throws ThemisException;

  public abstract void render(
      int frame, Scene scene, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore, VkFence fence)
      throws ThemisException;

  public abstract void resize(Scene scene) throws ThemisException;
}
