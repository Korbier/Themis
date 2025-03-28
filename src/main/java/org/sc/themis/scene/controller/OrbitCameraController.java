package org.sc.themis.scene.controller;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.sc.themis.input.Input;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.base.Camera;
import org.sc.themis.scene.base.Controller;
import org.sc.themis.scene.base.geometry.Instance;

import static org.lwjgl.glfw.GLFW.*;

public class OrbitCameraController implements Controller {

  private static final float MOUSE_SENSITIVITY = 0.5f;

  private final boolean inverseMouseX;
  private final boolean inverseMouseY;

  private final Scene scene;
  private Instance instance;

  public OrbitCameraController(Scene scene, Instance instance) {
    this(scene, false, false);
    this.instance = instance;
  }

  public OrbitCameraController(Scene scene, boolean inverseMouseX, boolean inverseMouseY) {
    this.inverseMouseX = inverseMouseX;
    this.inverseMouseY = inverseMouseY;
    this.scene = scene;
  }

  @Override
  public void update(long tpf) {}

  @Override
  public void input(Input input, long tpf) {
    if (input.isRightButtonPressed()) {
      Vector2f displVec = input.getDisplayVector();
      float sensitivityX = this.inverseMouseX ? (-1 * MOUSE_SENSITIVITY) : MOUSE_SENSITIVITY;
      float sensitivityY = this.inverseMouseY ? (-1 * MOUSE_SENSITIVITY) : MOUSE_SENSITIVITY;
     // this.instance.rotate(displVec.x * sensitivityX, 1.0f, 0.0f, 0.0f);
      this.instance.rotate(displVec.y * sensitivityY, 0.0f, 1.0f, 0.0f);
    }
  }
}
