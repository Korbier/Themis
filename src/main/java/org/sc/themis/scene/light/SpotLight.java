package org.sc.themis.scene.light;

import org.joml.Vector3f;
import org.sc.themis.shared.utils.MemorySizeUtils;

public class SpotLight extends Light {

    public final static int SIZE = Light.SIZE + MemorySizeUtils.VEC3F + MemorySizeUtils.VEC3F + MemorySizeUtils.VEC3F + MemorySizeUtils.FLOAT + MemorySizeUtils.FLOAT;

    private Vector3f position;
    private Vector3f direction;
    private Vector3f attenuation;
    private float innerCutOff;
    private float outerCutOff;

    public SpotLight(Vector3f ambient, Vector3f diffuse, Vector3f specular, Vector3f position, Vector3f direction, Vector3f attenuation, float innerCutOff, float outerCutOff ) {
        super(ambient, diffuse, specular);
        setPosition( position );
        setDirection( direction );
        setAttenuation( attenuation );
        setInnerCutOff( innerCutOff );
        setOuterCutOff( outerCutOff );
    }

    public Vector3f getPosition() {
        return new Vector3f( position );
    }

    public Vector3f getDirection() {
        return new Vector3f( direction );
    }

    public Vector3f getAttenuation() {
        return new Vector3f( attenuation );
    }

    public float getInnerCutOff() {
        return innerCutOff;
    }

    public float getOuterCutOff() {
        return outerCutOff;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
        setDirty();
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
        setDirty();
    }

    public void setAttenuation(Vector3f attenuation) {
        this.attenuation = attenuation;
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
