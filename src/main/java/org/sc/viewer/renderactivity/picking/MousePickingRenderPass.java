package org.sc.viewer.renderactivity.picking;

import org.sc.themis.renderer.sync.VkSemaphore;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.renderactivity.RenderPass;

public class MousePickingRenderPass extends RenderPass {

    public MousePickingRenderPass(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void setup() throws ThemisException {
    }

    @Override
    public void setup(Scene scene) throws ThemisException {

    }

    @Override
    public void cleanup() throws ThemisException {
    }

    @Override
    public void render(int frame, Scene scene, VkSemaphore waitSemaphore, VkSemaphore signalSemaphore) throws ThemisException {
    }

    @Override
    public void resize() throws ThemisException {

    }

}
