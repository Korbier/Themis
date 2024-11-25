package org.sc.themis.renderer;

import org.sc.themis.scene.Scene;
import org.sc.themis.shared.TObject;
import org.sc.themis.shared.exception.ThemisException;

public class Renderer extends TObject<RendererDescriptor> {

    public Renderer(RendererDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public void setup() throws ThemisException {

    }

    @Override
    public void cleanup() throws ThemisException {

    }

    public void render(Scene scene, long tpf ) {

    }

}
