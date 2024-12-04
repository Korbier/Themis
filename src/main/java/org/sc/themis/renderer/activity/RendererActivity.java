package org.sc.themis.renderer.activity;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public abstract class RendererActivity extends VulkanObject {

    public RendererActivity( Configuration configuration ) {
        super(configuration);
    }

    public abstract void setup( Renderer renderer ) throws ThemisException;
    public abstract void render(Scene scene, long tpf) throws ThemisException;
    public abstract void resize() throws ThemisException;

    @Override
    public final void setup() throws ThemisException {}

}
