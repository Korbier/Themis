package org.sc.themis.scene.factory;

import org.joml.Vector4f;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.renderer.resource.material.MaterialProperties;
import org.sc.themis.renderer.resource.material.MaterialProperty;
import org.sc.themis.renderer.base.resource.staging.VkStagingImage;

public class MaterialFactory {

  public Material color(Vector4f ambient, Vector4f diffuse, Vector4f specular, float shininess) {
    Material properties = new Material("color");
    properties.put(MaterialProperties.COLOR_AMBIENT, ambient);
    properties.put(MaterialProperties.COLOR_DIFFUSE, diffuse);
    properties.put(MaterialProperties.COLOR_SPECULAR, specular);
    properties.put(MaterialProperties.FLOAT_SHININESS, shininess);
    return properties;
  }

  public Material color(float r, float g, float b, float shininess) {
    Vector4f color = new Vector4f(r, g, b, 1.0f);
    return color(color, color, color, shininess);
  }

  public Material textureWithNormalMap(VkStagingImage texture, VkStagingImage normalMap) {
    Material properties = new Material("texture");
    properties.put(MaterialProperties.TEXTURE_ALBEDO, texture);
    properties.put(MaterialProperties.TEXTURE_NORMAL, normalMap);
    return properties;
  }

}
