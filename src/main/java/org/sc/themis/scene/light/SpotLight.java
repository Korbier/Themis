package org.sc.themis.scene.light;

import org.joml.Vector3f;
import org.sc.themis.scene.light.attenuation.Attenuation;
import org.sc.themis.shared.utils.MemorySizeUtils;

public final class SpotLight extends Light {

  public static final int SIZE =
      Light.SIZE
          + MemorySizeUtils.VEC4F
          + MemorySizeUtils.VEC4F
          + MemorySizeUtils.VEC4F
          + MemorySizeUtils.FLOAT
          + MemorySizeUtils.FLOAT;
  private final Vector3f position;
  private final Vector3f direction;
  private final Attenuation attenuation;
  private float innerCutOff;
  private float outerCutOff;

  public SpotLight(
      Vector3f ambient,
      Vector3f diffuse,
      Vector3f specular,
      Vector3f position,
      Vector3f direction,
      Attenuation attenuation,
      float innerCutOff,
      float outerCutOff) {
    super(ambient, diffuse, specular);
    this.position = new Vector3f(position);
    this.direction = new Vector3f(direction);
    this.attenuation = Attenuation.of(attenuation);
    setInnerCutOff(innerCutOff);
    setOuterCutOff(outerCutOff);
    setDirty();
  }

  public Vector3f getPosition() {
    return new Vector3f(position);
  }

  public Vector3f getDirection() {
    return new Vector3f(direction);
  }

  public Attenuation getAttenuation() {
    return Attenuation.of(attenuation);
  }

  public float getInnerCutOff() {
    return innerCutOff;
  }

  public float getOuterCutOff() {
    return outerCutOff;
  }

  public void setPosition(Vector3f position) {
    this.position.set(position);
    setDirty();
  }

  public void setDirection(Vector3f direction) {
    this.direction.set(direction);
    setDirty();
  }

  public void setAttenuation(Attenuation attenuation) {
    this.attenuation.set(attenuation);
    setDirty();
  }

  public void setInnerCutOff(float innerCutOff) {
    this.innerCutOff = innerCutOff;
    setDirty();
  }

  public void setOuterCutOff(float outerCutOff) {
    this.outerCutOff = outerCutOff;
    setDirty();
  }
}
