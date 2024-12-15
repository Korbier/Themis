package org.sc.themis.scene;

import org.jboss.logging.Logger;
import org.joml.*;

import java.lang.Math;

public class Instance {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(Instance.class);

    private final static Vector4f identifierReference = new Vector4f(0,0,0,1);
    private static float [] calculateIdentifier() {
        identifierReference.x += 0.01f;
        float [] aIdentifier = new float[4];
        aIdentifier[0] = identifierReference.x;
        aIdentifier[1] = identifierReference.y;
        aIdentifier[2] = identifierReference.z;
        aIdentifier[3] = identifierReference.w;
        LOG.tracef( "Provinding new instance identifier : [%f %f %f %f]", aIdentifier[0], aIdentifier[1], aIdentifier[2], aIdentifier[3] );
        return aIdentifier;
    }

    private final Model model;
    private final float [] identifier;

    private final Vector3f    position = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private float             scale    = 1.0f;
    private final Matrix4f    matrix   = new Matrix4f();

    private float [] fMatrix = new float[16];

    public Instance( Model model ) {
        this.model = model;
        this.identifier = calculateIdentifier();
        updateMatrix();
    }

    public Model getModel() {
        return model;
    }

    public float [] getIdentifier() {
        return this.identifier;
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
