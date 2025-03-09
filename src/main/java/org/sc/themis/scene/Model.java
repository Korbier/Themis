package org.sc.themis.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.sc.themis.renderer.material.MaterialProperties;
import org.sc.themis.shared.exception.ThemisException;

public class Model {

  private final String identifier;
  private final Mesh[] meshes;
  private final List<Instance> instances = new ArrayList<>();

  private MaterialProperties materialProperties = null;

  public Model(String identifier, Mesh[] meshes) {
    this.identifier = identifier;
    this.meshes = meshes;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void cleanup() throws ThemisException {
    for (Mesh mesh : getMeshes()) {
      mesh.cleanup();
    }
  }

  public boolean isRenderable() {

    for (Mesh mesh : this.getMeshes()) {
      if (!mesh.isRenderable()) {
        return false;
      }
    }

    return true;
  }

  public Instance create() {
    Instance instance = new Instance(this);
    this.instances.add(instance);
    return instance;
  }

  public Mesh[] getMeshes() {
    return this.meshes;
  }

  public List<Instance> getInstances() {
    return this.instances;
  }

  public void setMaterialProperties(MaterialProperties properties) {
    this.materialProperties = properties;
  }

  public MaterialProperties getMaterialProperties() {
    return this.materialProperties;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Model model = (Model) o;
    return Objects.equals(identifier, model.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(identifier);
  }
}
