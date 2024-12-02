package org.sc.themis.renderer.activity;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public abstract class RendererActivity extends VulkanObject {

    private Renderer renderer;

    public RendererActivity( Configuration configuration ) {
        super(configuration);
    }

    public Renderer getRenderer() {
        return this.renderer;
    }

    public abstract void setup( Renderer renderer ) throws ThemisException;

    @Override
    public final void setup() throws ThemisException {}

}
