package org.sc.themis.scene.light;

import org.joml.Vector4f;

public class Attenuation {

    private EnumAttenuation type;
    private final Vector4f data = new Vector4f();

    public static Attenuation of( Attenuation other ) {
        Attenuation attenuation = new Attenuation();
        attenuation.type = other.type;
        attenuation.data.set( other.data );
        return attenuation;
    }

    //https://lisyarus.github.io/blog/posts/point-light-attenuation.html
    public static Attenuation type1( float radius, float falloff ) {
        Attenuation attenuation = new Attenuation();
        attenuation.type = EnumAttenuation.TYPE_1;
        attenuation.data.set( 1.0f, radius, falloff, 0.0f);
        return  attenuation;
    }

    //https://lisyarus.github.io/blog/posts/point-light-attenuation.html
    public static Attenuation type2( float radius, float falloff ) {
        Attenuation attenuation = new Attenuation();
        attenuation.type = EnumAttenuation.TYPE_2;
        attenuation.data.set( 2.0f, radius, falloff, 0.0f);
        return  attenuation;
    }

    private Attenuation() {}

    public void set( Attenuation other ) {
        this.data.set( other.data );
    }

    public float getType() {
        return this.data.x;
    }

    public void setRadius( float newRadius ) {
        switch ( this.type ) {
            case  TYPE_1, TYPE_2 -> this.data.y = newRadius;
        }
    }

    public float getRadius() {
        return switch ( this.type ) {
            case  TYPE_1, TYPE_2 -> this.data.y;
        };
    }

    public void setFallOff( float newFallOff ) {
        switch ( this.type ) {
            case  TYPE_1, TYPE_2 -> this.data.z = newFallOff;
        }
    }

    public float getFallOff() {
        return switch ( this.type ) {
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

    public Vector4f data() {
        return this.data;
    }

}
