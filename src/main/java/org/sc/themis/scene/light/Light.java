package org.sc.themis.scene.light;

import org.joml.Vector3f;
import org.sc.themis.shared.utils.MemorySizeUtils;

public abstract class Light {

    public final static int SIZE = MemorySizeUtils.VEC4F + MemorySizeUtils.VEC4F + MemorySizeUtils.VEC4F;

    private boolean dirty = true;

    private final Vector3f ambient;
    private final Vector3f diffuse;
    private final Vector3f specular;

    private boolean visible = true;

    public Light( Vector3f ambient, Vector3f diffuse, Vector3f specular ) {
        this.ambient = new Vector3f( ambient );
        this.diffuse = new Vector3f( diffuse );
        this.specular = new Vector3f( specular );
        setDirty();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Vector3f getAmbient() {
        return new Vector3f(this.ambient);
    }

    public void setAmbient(Vector3f ambient) {
        this.ambient.set(ambient);
        setDirty();
    }

    public Vector3f getDiffuse() {
        return new Vector3f(this.diffuse);
    }

    public void setDiffuse(Vector3f diffuse) {
        this.diffuse.set(diffuse);
        setDirty();
    }

    public Vector3f getSpecular() {
        return new Vector3f(specular);
    }

    public void setSpecular(Vector3f specular) {
        this.specular.set(specular);
        setDirty();
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void clearDirtyState() {
        this.dirty = false;
    }

    protected void setDirty() {
        this.dirty = true;
    }

}
