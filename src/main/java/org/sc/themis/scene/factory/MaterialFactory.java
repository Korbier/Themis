package org.sc.themis.scene.factory;

import org.joml.Vector4f;
import org.sc.themis.renderer.material.MaterialProperties;
import org.sc.themis.renderer.material.MaterialProperty;

import java.util.Vector;

public class MaterialFactory {

    public MaterialProperties colored(float r, float g, float b, float shininess) {
        Vector4f color =  new Vector4f(r, g, b, 1.0f);
        MaterialProperties properties = new MaterialProperties();
        properties.put(MaterialProperty.Color.BASE, color);
        properties.put(MaterialProperty.Color.DIFFUSE, color);
        properties.put(MaterialProperty.Color.SPECULAR, color);
        properties.put(MaterialProperty.Property.SHININESS, shininess);
        return properties;
    }

}
