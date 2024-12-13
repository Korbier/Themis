package org.sc.themis.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Instance {

    private final Model model;

    private final Vector3f    position = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private float             scale    = 1.0f;
    private final Matrix4f    matrix   = new Matrix4f();

    private float [] fMatrix = new float[16];

    public Instance( Model model ) {
        this.model = model;
        updateMatrix();
    }

    public Model getModel() {
        return model;
    }

    public float[] matrix() {
        return this.fMatrix;
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

    public Instance rotate(float degAngle, float x, float y, float z ) {
        double angle = Math.toRadians( degAngle );
        this.rotation().rotateAxis( (float) angle, x, y, z );
        updateMatrix();
        return this;
    }

    public Instance scale(float scale ) {
        this.scale = scale;
        updateMatrix();
        return this;
    }

    private void updateMatrix() {
        this.matrix.translationRotateScale(position(), rotation(), scale());
        this.fMatrix = this.matrix.get( this.fMatrix );
    }

}
