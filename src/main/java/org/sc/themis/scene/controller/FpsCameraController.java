package org.sc.themis.scene.controller;

import org.joml.Vector2f;
import org.sc.themis.input.Input;
import org.sc.themis.scene.Camera;
import org.sc.themis.scene.Controller;
import org.sc.themis.scene.Scene;

import static org.lwjgl.glfw.GLFW.*;

public class FpsCameraController implements Controller {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.005f;

    private final boolean inverseMouseX;
    private final boolean inverseMouseY;

    private final Scene scene;

    public FpsCameraController(Scene scene) {
        this( scene, false, false );
    }

    public FpsCameraController(Scene scene, boolean inverseMouseX, boolean inverseMouseY ) {
        this.inverseMouseX = inverseMouseX;
        this.inverseMouseY = inverseMouseY;
        this.scene = scene;
    }

    @Override
    public void update( long tpf) {}

    @Override
    public void input( Input input, long tpf ) {

        float move = tpf * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();

        if ( input.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(move);
        } else if (input.isKeyPressed(GLFW_KEY_S)) {
            camera.moveBackwards(move);
        }

        if (input.isKeyPressed(GLFW_KEY_A)) {
            camera.moveLeft(move);
        } else if (input.isKeyPressed(GLFW_KEY_D)) {
            camera.moveRight(move);
        }

        if (input.isKeyPressed(GLFW_KEY_UP)) {
            camera.moveUp(move);
        } else if (input.isKeyPressed(GLFW_KEY_DOWN)) {
            camera.moveDown(move);
        }

        if ( input.isRightButtonPressed() ) {
            Vector2f displVec = input.getDisplayVector();
            float sensitivityX = this.inverseMouseX ? (-1 * MOUSE_SENSITIVITY) : MOUSE_SENSITIVITY;
            float sensitivityY = this.inverseMouseY ? (-1 * MOUSE_SENSITIVITY) : MOUSE_SENSITIVITY;
            camera.addRotationDeg( displVec.x * sensitivityX, displVec.y * sensitivityY);
        }

    }

}
