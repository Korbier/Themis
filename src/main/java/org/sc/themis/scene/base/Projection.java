package org.sc.themis.scene.base;

import org.joml.Matrix4f;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.configuration.ConfigurationEnum;

public class Projection {

  private final Matrix4f perspective;
  private final Matrix4f orthographic;

  private float fov;
  private float znear;
  private float zfar;

  public Projection(float fov, float znear, float zfar) {
    this.perspective = new Matrix4f();
    this.orthographic = new Matrix4f();
    this.fov = fov;     //this.configuration.get(ConfigurationEnum.sceneProjectionFov, 60.0f);
    this.znear = znear; //this.configuration.get(ConfigurationEnum.sceneProjectionZNear, 0.1f);
    this.zfar = zfar;   //this.configuration.get(ConfigurationEnum.sceneProjectionZFar, 1400.0f);
  }

  public Matrix4f perspective() {
    return perspective;
  }

  public Matrix4f orthographic() {
    return this.orthographic;
  }

  public float fov() {
    return fov;
  }

  public float znear() {
    return znear;
  }

  public float zfar() {
    return zfar;
  }

  public void resize(int width, int height) {
    perspective().identity();
    perspective().perspective((float) Math.toRadians(this.fov), (float) width / (float) height, this.znear, this.zfar, true);
    orthographic().identity();
    orthographic().ortho(0, (float) width, (float) height, 0, this.znear, this.zfar, true);
  }
}
