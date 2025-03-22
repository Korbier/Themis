package org.sc.themis.scene.base.geometry;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Instance {

  private static final Logger logger = LoggerFactory.getLogger(Instance.class);

  private static final Vector4f identifierReference = new Vector4f(0, 0, 0, 1);

  private static float[] calculateIdentifier() {
    identifierReference.x += 0.000001f;
    float[] identifier = new float[4];
    identifier[0] = identifierReference.x;
    identifier[1] = identifierReference.y;
    identifier[2] = identifierReference.z;
    identifier[3] = identifierReference.w;
    logger.trace(
        "Provinding new instance identifier : [{} {} {} {}]",
        identifier[0], identifier[1], identifier[2], identifier[3]);
    return identifier;
  }

  private final Model model;
  private final float[] identifier;

  private final Vector3f position = new Vector3f();
  private final Quaternionf rotation = new Quaternionf();
  private float scale = 1.0f;
  private final Matrix4f matrix = new Matrix4f();

  private float[] matrixAsFloats = new float[16];

  public Instance(Model model) {
    this.model = model;
    this.identifier = calculateIdentifier();
    updateMatrix();
  }

  public Model getModel() {
    return model;
  }

  public float[] getIdentifier() {
    return this.identifier;
  }

  public float[] matrix() {
    return this.matrixAsFloats;
  }

  public Vector3f position() {
    return this.position;
  }

  public Quaternionf rotation() {
    return this.rotation;
  }

  public float scale() {
    return this.scale;
  }

  public Instance position(float x, float y, float z) {
    position.x = x;
    position.y = y;
    position.z = z;
    updateMatrix();
    return this;
  }

  public Instance rotate(float degAngle, float x, float y, float z) {
    double angle = Math.toRadians(degAngle);
    this.rotation().rotateAxis((float) angle, x, y, z);
    updateMatrix();
    return this;
  }

  public Instance scale(float scale) {
    this.scale = scale;
    updateMatrix();
    return this;
  }

  private void updateMatrix() {
    this.matrix.translationRotateScale(position(), rotation(), scale());
    this.matrixAsFloats = this.matrix.get(this.matrixAsFloats);
  }
}
