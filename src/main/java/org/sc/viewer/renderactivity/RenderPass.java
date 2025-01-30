package org.sc.viewer.renderactivity;

import org.lwjgl.vulkan.VkExtent2D;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.resource.image.VkImageView;
import org.sc.themis.renderer.sync.VkSemaphore;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public abstract class RenderPass extends VulkanObject  {

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

    protected VkImageView getImageView( int frame ) {
        return this.viewerActivity.getRenderer().getImageView( frame );
    }

    public final void setup( ViewerRendererActivity activity ) throws ThemisException {
        this.viewerActivity = activity;
        this.setup();
    }

    public ViewerRendererActivity getViewerActivity() {
        return this.viewerActivity;
    }

    abstract public void setup(Scene scene) throws ThemisException;

    abstract public void render(int frame, Scene scene, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore ) throws ThemisException;

    abstract public void resize() throws ThemisException;

}
