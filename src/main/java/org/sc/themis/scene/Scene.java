package org.sc.themis.scene;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.shared.TObject;

public class Scene extends TObject<SceneDescriptor> {

    private static final Logger LOG = Logger.getLogger(Scene.class);

    private final Renderer renderer;

    public Scene(Renderer renderer, SceneDescriptor descriptor) {
        super(descriptor);
        this.renderer = renderer;
    }

    @Override
    public void setup() {

        LOG.trace( "Scene initialized" );
    }

    @Override
    public void cleanup() {

    }

}
