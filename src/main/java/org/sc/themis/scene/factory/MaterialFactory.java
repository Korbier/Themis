package org.sc.themis.scene.factory;

import org.joml.Vector4f;
import org.sc.themis.renderer.material.MaterialProperties;
import org.sc.themis.renderer.material.MaterialProperty;

public class MaterialFactory {

    public MaterialProperties colored( float r, float g, float b ) {
        MaterialProperties properties = new MaterialProperties();
        properties.put(MaterialProperty.Color.BASE, new Vector4f(r, g, b, 1.0f ) );
        return properties;
    }

}
