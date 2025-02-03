package org.sc.themis.scene;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.material.MaterialProperties;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.scene.light.SpotLight;
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
    private final List<MaterialProperties> materialProperties = new ArrayList<>();

    /** Light casters **/
    private final List<DirectionalLight> directionalLights = new ArrayList<>();
    private final List<SpotLight> spotLights = new ArrayList<>();

    /** Controller **/
    private final Set<Controller> controllers = new HashSet<>();

    /** Material **/
    private final Map<String, String> materials = new HashMap<>();

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
        add( null, instances );
    }

    public void add( String defaultMaterial, Instance ... instances ) {
        for ( Instance instance : instances ) {
            add( instance.getModel(), defaultMaterial );
            this.instances.add( instance );
        }
    }

    public void add( Controller ... controllers ) {
        Collections.addAll(this.controllers, controllers);
    }

    public void add( DirectionalLight light ) {
        this.directionalLights.add( light );
    }

    public void add( SpotLight light ) {
        this.spotLights.add( light );
    }

    private void add( Model model, String material ) {

        this.models.add( model );

        if ( material != null ) {
            this.materials.put( model.getIdentifier(), material );
        }

        for ( Mesh mesh : model.getMeshes() ) {
            this.materialProperties.add( mesh.getProperties() );
        }

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

    public String getMaterial( Model model ) {
        return this.materials.get( model.getIdentifier() );
    }

    public List<DirectionalLight> getDirectionalLights() {
        return Collections.unmodifiableList( this.directionalLights );
    }

    public List<SpotLight> getSpotLights() {
        return Collections.unmodifiableList( this.spotLights );
    }

    public MaterialProperties [] getMaterialsProperties() {

        Set<MaterialProperties> materialProperties = new HashSet<>();

        for ( Model model : getModels() ) {

            materialProperties.add( model.getMaterialProperties() );

            for ( Mesh mesh : model.getMeshes() ) {
                materialProperties.add( mesh.getProperties() );
            }

        }

        return materialProperties.toArray( new MaterialProperties[0] );

    }

}
