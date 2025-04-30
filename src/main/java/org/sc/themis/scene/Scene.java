package org.sc.themis.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joml.Vector4f;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.scene.base.Camera;
import org.sc.themis.scene.base.Controller;
import org.sc.themis.scene.base.Projection;
import org.sc.themis.renderer.resource.model.Instance;
import org.sc.themis.renderer.resource.model.Mesh;
import org.sc.themis.renderer.resource.model.Model;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.scene.light.PointLight;
import org.sc.themis.scene.light.SpotLight;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.configuration.ConfigurationEnum;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Scene. */
public class Scene implements LifeCycle {

  private static final Logger logger = LoggerFactory.getLogger(Scene.class);

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

  /**
   * Default constructor.
   *
   * @param configuration Main configuration.
   */
  public Scene(Configuration configuration) {
    this.projection = new Projection(
      configuration.get(ConfigurationEnum.sceneProjectionFov, 60.0f),
      configuration.get(ConfigurationEnum.sceneProjectionZNear, 0.1f),
      configuration.get(ConfigurationEnum.sceneProjectionZFar, 1400.0f)
    );
    this.camera = new Camera();
  }

  @Override
  public void setup() {
    logger.trace("Scene initialized");
  }

  @Override
  public void cleanup() throws ThemisException {}

  /**
   * Add model instances to the scene.
   *
   * @param instances Added instances.
   */
  public void add(Instance... instances) {
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
  public void add(Controller... controllers) {
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
        0.0f);
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
  public Material[] getMaterialsProperties() {

    Set<Material> materialProperties = new HashSet<>();

    for (Model model : getModels()) {

      materialProperties.add(model.getMaterial());

      for (Mesh mesh : model.getMeshes()) {
        materialProperties.add(mesh.getProperties());
      }
    }

    return materialProperties.toArray(new Material[0]);
  }
}
