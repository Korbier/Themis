package org.sc.themis.scene;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.shared.TObject;
import org.sc.themis.shared.exception.ThemisException;

public class Scene extends TObject<SceneDescriptor> {

    private final Renderer renderer;

    public Scene(Renderer renderer, SceneDescriptor descriptor) {
        super(descriptor);
        this.renderer = renderer;
    }

    @Override
    public void setup() throws ThemisException {

    }

    @Override
    public void cleanup() throws ThemisException {

    }

}
