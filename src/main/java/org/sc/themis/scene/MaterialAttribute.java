package org.sc.themis.scene;

public interface MaterialAttribute {

    enum Color implements MaterialAttribute {
        BASE,
        DIFFUSE,
        EMISSIVE,
        SPECULAR,
        SHININESS
    }

    enum Texture implements MaterialAttribute {

    }

}
