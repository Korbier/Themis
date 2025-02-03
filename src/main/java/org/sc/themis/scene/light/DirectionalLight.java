package org.sc.themis.scene.light;

import org.joml.Vector3f;
import org.sc.themis.shared.utils.MemorySizeUtils;

public class DirectionalLight extends Light {

    public final static int SIZE = Light.SIZE + MemorySizeUtils.VEC3F;

    private Vector3f direction;

    public DirectionalLight(Vector3f ambient, Vector3f diffuse, Vector3f specular, Vector3f direction) {
        super(ambient, diffuse, specular);
        setDirection( direction );
    }

    public void setDirection( Vector3f vector3f ) {
        this.direction = vector3f;
        setDirty();
    }

    public Vector3f getDirection() {
        return new Vector3f( this.direction );
    }

}
