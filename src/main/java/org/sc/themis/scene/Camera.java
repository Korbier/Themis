package org.sc.themis.scene;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

    private final Vector3f direction;
    private final Vector3f position;
    private final Vector3f right;
    private final Vector2f rotation;
    private final Vector3f up;
    private final Matrix4f viewMatrix;

    private final Vector3f front;

    public Camera() {
        this.direction = new Vector3f();
        this.right = new Vector3f();
        this.up = new Vector3f();
        this.position = new Vector3f();
        this.viewMatrix = new Matrix4f();
        this.rotation = new Vector2f();
        this.front = new Vector3f();
    }

    public Vector3f getFront() {
        return this.front;
    }

    public void addRotation(float x, float y) {
        rotation.add(x, y);
        recalculate();
    }

    public void addRotationDeg(float x, float y) {
        rotation.add((float) Math.toRadians(x), (float) Math.toRadians(y));
        recalculate();
    }

    public Vector3f getPosition() {
        return position;
    }

    public Matrix4f matrix() {
        return viewMatrix;
    }

    public void moveBackwards(float inc) {
        viewMatrix.positiveZ(direction).negate().mul(inc);
        position.sub(direction);
        recalculate();
    }

    public void moveDown(float inc) {
        viewMatrix.positiveY(up).mul(inc);
        position.sub(up);
        recalculate();
    }

    public void moveForward(float inc) {
        viewMatrix.positiveZ(direction).negate().mul(inc);
        position.add(direction);
        recalculate();
    }

    public void moveLeft(float inc) {
        viewMatrix.positiveX(right).mul(inc);
        position.sub(right);
        recalculate();
    }

    public void moveRight(float inc) {
        viewMatrix.positiveX(right).mul(inc);
        position.add(right);
        recalculate();
    }

    public void moveUp(float inc) {
        viewMatrix.positiveY(up).mul(inc);
        position.add(up);
        recalculate();
    }

    private void recalculate() {

        viewMatrix.identity()
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .translate(-position.x, -position.y, -position.z);

        // https://community.khronos.org/t/get-direction-from-transformation-matrix-or-quat/65502/3
        this.viewMatrix.getRow(2, this.direction);
        this.front.set( -this.direction.x, -this.direction.y , -this.direction.z );

    }

    public void setPosition(Vector3f position) {
        this.setPosition( position.x, position.y, position.z);
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        recalculate();
    }

    public void setRotation(float x, float y) {
        rotation.set(x, y);
        recalculate();
    }

    public void setRotationDeg(float x, float y) {
        rotation.set((float) Math.toRadians(x), (float) Math.toRadians(y));
        recalculate();
    }

}
