package org.sc.themis.scene.base;

import org.joml.Matrix4f;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.configuration.ConfigurationEnum;

public class Projection {

  private final Configuration configuration;
  private final Matrix4f perspective;
  private final Matrix4f orthographic;

  public Projection(Configuration configuration) {
    this.configuration = configuration;
    this.perspective = new Matrix4f();
    this.orthographic = new Matrix4f();
  }

  public Matrix4f perspective() {
    return perspective;
  }

  public Matrix4f orthographic() {
    return this.orthographic;
  }

  public void resize(int width, int height) {

    float fov = this.configuration.get(ConfigurationEnum.sceneProjectionFov, 60.0f);
    float znear = this.configuration.get(ConfigurationEnum.sceneProjectionZNear, 0.1f);
    float zfar = this.configuration.get(ConfigurationEnum.sceneProjectionZFar, 1400.0f);

    perspective().identity();
    perspective()
        .perspective(
            (float) Math.toRadians(fov), (float) width / (float) height, znear, zfar, true);

    orthographic().identity();
    orthographic().ortho(0, (float) width, (float) height, 0, znear, zfar, true);
  }
}
