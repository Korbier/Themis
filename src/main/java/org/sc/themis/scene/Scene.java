package org.sc.themis.scene;

import java.util.*;

import org.jboss.logging.Logger;
import org.joml.Vector4f;
import org.sc.themis.renderer.material.MaterialProperties;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.scene.light.PointLight;
import org.sc.themis.scene.light.SpotLight;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

/**
 * Scene.
 */
public class Scene extends TObject {

    private static final Logger LOG = Logger.getLogger(Scene.class);

    // Camera and projection
    private final Projection projection;
    private final Camera camera;

    // Geometry
    private final List<Instance> instances = new ArrayList<>();
    private final Set<Model> models = new HashSet<>();

    // Light casters
    private final List<DirectionalLight> directionalLights = new ArrayList<>();
    private final List<PointLight> pointLights = new ArrayList<>();
    private final List<SpotLight> spotLights = new ArrayList<>();

    // Controller
    private final Set<Controller> controllers = new HashSet<>();

    //Pencil
    private final Pencil pencil = new Pencil();

    /**
     * Default constructor.
     *
     * @param configuration Main configuration.
     **/
    public Scene(Configuration configuration) {
        super(configuration);
        this.projection = new Projection(configuration);
        this.camera = new Camera();
    }

    @Override
    public void setup() {
        LOG.trace("Scene initialized");
    }

    @Override
    public void cleanup() throws ThemisException {
    }

    /**
     * Add model instances to the scene.
     *
     * @param instances Added instances.
     */
    public void add(Instance ... instances) {
        for (Instance instance : instances) {
            this.models.add(instance.getModel());
            this.instances.add(instance);
        }
    }

    /**
     * Add scene controllers to the scene.
     *
     * @param controllers Added controllers.
     */
    public void add(Controller ... controllers) {
        Collections.addAll(this.controllers, controllers);
    }

    /**
     * Add a directional light to the scene.
     *
     * @param light Added light.
     */
    public void add(DirectionalLight light) {
        this.directionalLights.add(light);
    }

    /**
     * Add a spot light to the scene.
     *
     * @param light Added light.
     */
    public void add(SpotLight light) {
        this.spotLights.add(light);
    }

    /**
     * Add a point light to the scene.
     *
     * @param light Added light.
     */
    public void add(PointLight light) {
        this.pointLights.add(light);
    }

    public Pencil getPencil() {
        return this.pencil;
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

    public Vector4f getLightData() {
        return new Vector4f(
            (float) getDirectionalLights().size(),
            (float) getPointLights().size(),
            (float) getSpotLights().size(),
            0.0f
       );
    }

    public List<DirectionalLight> getDirectionalLights() {
        return Collections.unmodifiableList(this.directionalLights);
    }

    public List<SpotLight> getSpotLights() {
        return Collections.unmodifiableList(this.spotLights);
    }

    public List<PointLight> getPointLights() {
        return Collections.unmodifiableList(this.pointLights);
    }

    /**
     * Retrieve all material properties.
     *
     * @return Material propertis
     */
    public MaterialProperties [] getMaterialsProperties() {

        Set<MaterialProperties> materialProperties = new HashSet<>();

        for (Model model : getModels()) {

            materialProperties.add(model.getMaterialProperties());

            for (Mesh mesh : model.getMeshes()) {
                materialProperties.add(mesh.getProperties());
            }

        }

        return materialProperties.toArray(new MaterialProperties[0]);

    }

}
