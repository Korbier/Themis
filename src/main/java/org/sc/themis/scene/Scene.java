package org.sc.themis.scene;

import org.jboss.logging.Logger;
import org.sc.themis.scene.material.Material;
import org.sc.themis.scene.material.MaterialAllocator;
import org.sc.themis.scene.material.MaterialProperties;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

import java.util.*;
import java.util.stream.Stream;

public class Scene extends TObject {

    private static final Logger LOG = Logger.getLogger(Scene.class);

    /** Camera and projection **/
    private final Projection projection;
    private final Camera camera;

    /** Geometry **/
    private final List<Instance> instances = new ArrayList<>();
    private final Set<Model> models = new HashSet<>();
    private final List<MaterialProperties> materialProperties = new ArrayList<>();

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

    public void allocateMaterial( MaterialAllocator materialAllocator ) throws ThemisException {

        if ( this.materialProperties.isEmpty() ) {
            return;
        }

        for ( MaterialProperties properties : this.materialProperties) {
            materialAllocator.allocate( properties );
        }

        this.materialProperties.clear();

    }

    private void add(Model model) {

        this.models.add( model );

        for ( Mesh mesh : model.getMeshes() ) {
            this.materialProperties.add( mesh.getProperties() );
        }

    }

    public Set<Model> getModels() {
        return this.models;
    }

    public Stream<Model> getModels( Material material ) {
        return getModels().stream().filter( m -> m.getMeshesAsStream().anyMatch( mesh -> material.getIdentifier().equals(mesh.getMaterialIdentifier()) ) );
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
