package org.sc.themis.scene.factory;

import org.joml.Vector4f;
import org.sc.themis.renderer.material.MaterialProperties;
import org.sc.themis.renderer.material.MaterialProperty;
import org.sc.themis.renderer.resource.VkStagingImage;
import org.sc.themis.shared.resource.Image;

public class MaterialFactory {

  public MaterialProperties color(
      Vector4f ambient, Vector4f diffuse, Vector4f specular, float shininess) {
    MaterialProperties properties = new MaterialProperties();
    properties.put(MaterialProperty.Color.BASE, ambient);
    properties.put(MaterialProperty.Color.DIFFUSE, diffuse);
    properties.put(MaterialProperty.Color.SPECULAR, specular);
    properties.put(MaterialProperty.Property.SHININESS, shininess);
    return properties;
  }

  public MaterialProperties color(float r, float g, float b, float shininess) {
    Vector4f color = new Vector4f(r, g, b, 1.0f);
    return color(color, color, color, shininess);
  }

  public MaterialProperties textureWithNormalMap(VkStagingImage texture, VkStagingImage normalMap) {
    MaterialProperties properties = new MaterialProperties();
    properties.put(MaterialProperty.Texture.BASE, texture);
    properties.put(MaterialProperty.Texture.NORMALS, normalMap);
    return properties;
  }

}
