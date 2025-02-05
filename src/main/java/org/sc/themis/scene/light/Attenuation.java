package org.sc.themis.scene.light;

import org.joml.Vector4f;

/**
 * Light attenuation.
 */
public class Attenuation {

    private EnumAttenuation type;
    private final Vector4f data = new Vector4f();

    /**
     * Create an attenuation from another (copy).
     *
     * @param other Other attenuation to copy from
     * @return A new attenuation
     */
    public static Attenuation of(Attenuation other) {
        Attenuation attenuation = new Attenuation();
        attenuation.type = other.type;
        attenuation.data.set(other.data);
        return attenuation;
    }

    /**
     *  Create an attenuation of type 1.
     *  Documentation : <a href=" https://lisyarus.github.io/blog/posts/point-light-attenuation.html">here</a>
     *
     * @param radius Attenuation radius
     * @param falloff Attenuation falloff
     * @return A new attenuation
     */
    public static Attenuation type1(float radius, float falloff) {
        Attenuation attenuation = new Attenuation();
        attenuation.type = EnumAttenuation.TYPE_1;
        attenuation.data.set(1.0f, radius, falloff, 0.0f);
        return  attenuation;
    }

    /**
     *  Create an attenuation of type 2.
     *  Documentation : <a href=" https://lisyarus.github.io/blog/posts/point-light-attenuation.html">here</a>
     *
     * @param radius Attenuation radius
     * @param falloff Attenuation falloff
     * @return A new attenuation
     */
    public static Attenuation type2(float radius, float falloff) {
        Attenuation attenuation = new Attenuation();
        attenuation.type = EnumAttenuation.TYPE_2;
        attenuation.data.set(2.0f, radius, falloff, 0.0f);
        return  attenuation;
    }

    private Attenuation() {}

    /**
     *  Set data from another attenuation object.
     *
     * @param other Source attenuation
     */
    public void set(Attenuation other) {
        this.data.set(other.data);
    }

    public float getType() {
        return this.data.x;
    }

    /**
     *  Set radius.
     *
     * @param newRadius new radius value
     */
    public void setRadius(float newRadius) {
        switch (this.type) {
            case  TYPE_1, TYPE_2 -> this.data.y = newRadius;
            default -> {}
        }
    }

    public float getRadius() {
        return switch (this.type) {
            case  TYPE_1, TYPE_2 -> this.data.y;
            default -> -0.0f;
        };
    }

    /**
     *  Set fallof.
     *
     * @param newFallOff new fallof value
     */
    public void setFallOff(float newFallOff) {
        switch (this.type) {
            case  TYPE_1, TYPE_2 -> this.data.z = newFallOff;
            default -> {}
        }
    }

    public float getFallOff() {
        return switch (this.type) {
            case  TYPE_1, TYPE_2 -> this.data.z;
        };
    }

    public float getData1() {
        return this.data.y;
    }

    public float getData2() {
        return this.data.z;
    }

    public float getData3() {
        return this.data.w;
    }

    /**
     *  Return raw data.
     *
     * @return raw data
     *
     */
    public Vector4f data() {
        return this.data;
    }

}
