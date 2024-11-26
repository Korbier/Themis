package org.sc.themis.scene;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.tobject.TObject;

public class Scene extends TObject {

    private static final Logger LOG = Logger.getLogger(Scene.class);

    private final Renderer renderer;

    public Scene( Configuration configuration, Renderer renderer ) {
        super(configuration);
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
