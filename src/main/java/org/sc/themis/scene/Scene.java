package org.sc.themis.scene;

import org.jboss.logging.Logger;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

import java.util.*;

public class Scene extends TObject {

    private static final Logger LOG = Logger.getLogger(Scene.class);

    /** Camera and projection **/
    private final Projection projection;
    private final Camera camera;

    /** Geometry **/
    private final List<Instance> instances = new ArrayList<>();
    private final Set<Model> models = new HashSet<>();

    /** Controller **/
    private final Set<Controller> controllers = new HashSet<>();

    public Scene( Configuration configuration ) {
        super(configuration);
        this.projection = new Projection( configuration );
        this.camera = new Camera();
    }

    @Override
    public void setup() {
        LOG.trace( "Scene initialized" );
    }

    @Override
    public void cleanup() throws ThemisException {
        for (Model model : this.models) {
            model.cleanup();
        }
    }

    public void add( Instance ... instances ) {
        for ( Instance instance : instances ) {
            add( instance.getModel() );
            this.instances.add( instance );
        }
    }

    public void add( Controller ... controllers ) {
        Collections.addAll(this.controllers, controllers);
    }

    private void add(Model model) {
        this.models.add( model );
    }

    public Set<Model> getModels() {
        return this.models;
    }

    public Projection getProjection() {
        return this.projection;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public Set<Controller> getControllers() {
        return this.controllers;
    }

}
