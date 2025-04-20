package org.sc.themis.renderer.resource.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.joml.Vector3f;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.shared.exception.ThemisException;

public class Model {

  private final String identifier;
  private final Mesh[] meshes;
  private final List<Instance> instances = new ArrayList<>();

  private Material material = null;
  private String materialRenderer = null;
  private Vector3f originalSize = null;

  public Model(String identifier, Mesh[] meshes) {
    this(identifier, meshes, null);
  }

  public Model(String identifier, Mesh[] meshes, Vector3f originalSize) {
    this.identifier = identifier;
    this.meshes = meshes;
    this.originalSize = originalSize;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public Optional<String> getMaterialRenderer() {
    return Optional.ofNullable(this.materialRenderer);
  }

  public Vector3f getOriginalSize() {
    return this.originalSize;
  }

  public void setMaterialRenderer(String materialRendererIdentifier) {
    this.materialRenderer = materialRendererIdentifier;
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

  public void setMaterial(Material properties) {
    this.material = properties;
  }

  public Material getMaterial() {
    return this.material;
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
