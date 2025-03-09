package org.sc.themis.scene.light;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.sc.themis.shared.utils.MemorySizeUtils;

public abstract sealed class Light permits DirectionalLight, PointLight, SpotLight {

  public static final int SIZE =
      MemorySizeUtils.VEC4F + MemorySizeUtils.VEC4F + MemorySizeUtils.VEC4F + MemorySizeUtils.VEC4F;

  private boolean dirty = true;

  private final Vector3f ambient;
  private final Vector3f diffuse;
  private final Vector3f specular;

  private final Vector4f data = new Vector4f(1.0f, 0.0f, 0.0f, 0.0f);

  public Light(Vector3f ambient, Vector3f diffuse, Vector3f specular) {
    this.ambient = new Vector3f(ambient);
    this.diffuse = new Vector3f(diffuse);
    this.specular = new Vector3f(specular);
    setDirty();
  }

  public boolean isVisible() {
    return this.data.x == 1.0f;
  }

  public void setVisible(boolean visible) {
    this.data.x = visible ? 1.0f : 0.0f;
  }

  public Vector4f getData() {
    return this.data;
  }

  public Vector3f getAmbient() {
    return new Vector3f(this.ambient);
  }

  public void setAmbient(Vector3f ambient) {
    this.ambient.set(ambient);
    setDirty();
  }

  public Vector3f getDiffuse() {
    return new Vector3f(this.diffuse);
  }

  public void setDiffuse(Vector3f diffuse) {
    this.diffuse.set(diffuse);
    setDirty();
  }

  public Vector3f getSpecular() {
    return new Vector3f(specular);
  }

  public void setSpecular(Vector3f specular) {
    this.specular.set(specular);
    setDirty();
  }

  public boolean isDirty() {
    return this.dirty;
  }

  public void clearDirtyState() {
    this.dirty = false;
  }

  protected void setDirty() {
    this.dirty = true;
  }
}
