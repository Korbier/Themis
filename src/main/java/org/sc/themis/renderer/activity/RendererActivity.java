package org.sc.themis.renderer.activity;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

public abstract class RendererActivity extends TObject {

    public RendererActivity(Configuration configuration) {
        super(configuration);
    }

    public abstract void setup(Renderer renderer) throws ThemisException;
    public abstract void render(Scene scene, long tpf) throws ThemisException;
    public abstract void resize(Scene scene) throws ThemisException;

    public void setup(Scene scene) throws ThemisException {}

    @Override
    public final void setup() throws ThemisException {}

}
