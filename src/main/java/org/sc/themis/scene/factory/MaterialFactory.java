package org.sc.themis.scene.factory;

import org.joml.Vector4f;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.renderer.resource.material.MaterialProperty;
import org.sc.themis.renderer.base.resource.staging.VkStagingImage;

public class MaterialFactory {

  public Material color(
      Vector4f ambient, Vector4f diffuse, Vector4f specular, float shininess) {
    Material properties = new Material();
    properties.put(MaterialProperty.Color.BASE, ambient);
    properties.put(MaterialProperty.Color.DIFFUSE, diffuse);
    properties.put(MaterialProperty.Color.SPECULAR, specular);
    properties.put(MaterialProperty.Property.SHININESS, shininess);
    return properties;
  }

  public Material color(float r, float g, float b, float shininess) {
    Vector4f color = new Vector4f(r, g, b, 1.0f);
    return color(color, color, color, shininess);
  }

  public Material textureWithNormalMap(VkStagingImage texture, VkStagingImage normalMap) {
    Material properties = new Material();
    properties.put(MaterialProperty.Texture.BASE, texture);
    properties.put(MaterialProperty.Texture.NORMALS, normalMap);
    return properties;
  }

}
