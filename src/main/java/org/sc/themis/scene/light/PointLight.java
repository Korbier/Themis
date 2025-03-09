package org.sc.themis.scene.light;

import org.joml.Vector3f;
import org.sc.themis.scene.light.attenuation.Attenuation;
import org.sc.themis.shared.utils.MemorySizeUtils;

public final class PointLight extends Light {

  public static final int SIZE = Light.SIZE + MemorySizeUtils.VEC4F + MemorySizeUtils.VEC4F;

  private final Vector3f position;
  private final Attenuation attenuation;

  public PointLight(
      Vector3f ambient,
      Vector3f diffuse,
      Vector3f specular,
      Vector3f position,
      Attenuation attenuation) {
    super(ambient, diffuse, specular);
    this.attenuation = Attenuation.of(attenuation);
    this.position = new Vector3f(position);
    setDirty();
  }

  public Vector3f getPosition() {
    return new Vector3f(position);
  }

  public void setPosition(Vector3f position) {
    this.position.set(position);
    setDirty();
  }

  public Attenuation getAttenuation() {
    return Attenuation.of(attenuation);
  }

  public void setAttenuation(Attenuation attenuation) {
    this.attenuation.set(attenuation);
    setDirty();
  }
}
