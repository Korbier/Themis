package org.sc.themis.renderer;

import org.jboss.logging.Logger;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.TObject;
import org.sc.themis.shared.exception.ThemisException;

public class Renderer extends TObject<RendererDescriptor> {

    private static final Logger LOG = Logger.getLogger(Renderer.class);

    public Renderer(RendererDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public void setup() {

        LOG.trace( "Renderer initialized" );
    }

    @Override
    public void cleanup() {

    }

    public void render(Scene scene, long tpf ) {

    }

}
