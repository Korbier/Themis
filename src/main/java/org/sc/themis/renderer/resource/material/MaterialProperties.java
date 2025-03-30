package org.sc.themis.renderer.resource.material;


import org.joml.Vector4f;
import org.sc.themis.renderer.base.resource.staging.VkStagingImage;

import java.util.HashMap;
import java.util.Map;

public class MaterialProperties {

  public static Map<String, MaterialProperty<?>> properties = new HashMap<>();

  public final static String PROPERTY_NAME_COLOR_AMBIENT  = "color.ambient";
  public final static String PROPERTY_NAME_COLOR_DIFFUSE  = "color.diffuse";
  public final static String PROPERTY_NAME_COLOR_SPECULAR = "color.specular";
  public final static String PROPERTY_NAME_COLOR_EMISSIVE = "color.emissive";

  public final static String PROPERTY_NAME_TEXTURE_ALBEDO = "texture.albedo";
  public final static String PROPERTY_NAME_TEXTURE_NORMAL = "texture.normal";
  public final static String PROPERTY_NAME_FLOAT_SHININESS = "float.shininess";

  public final static MaterialProperty<Vector4f> COLOR_AMBIENT = MaterialProperty.of(Vector4f.class, PROPERTY_NAME_COLOR_AMBIENT);
  public final static MaterialProperty<Vector4f> COLOR_DIFFUSE = MaterialProperty.of(Vector4f.class, PROPERTY_NAME_COLOR_DIFFUSE);
  public final static MaterialProperty<Vector4f> COLOR_SPECULAR = MaterialProperty.of(Vector4f.class, PROPERTY_NAME_COLOR_SPECULAR);
  public final static MaterialProperty<Vector4f> COLOR_EMISSIVE = MaterialProperty.of(Vector4f.class, PROPERTY_NAME_COLOR_EMISSIVE);

  public final static MaterialProperty<VkStagingImage> TEXTURE_ALBEDO = MaterialProperty.of(VkStagingImage.class, PROPERTY_NAME_TEXTURE_ALBEDO);
  public final static MaterialProperty<VkStagingImage> TEXTURE_NORMAL = MaterialProperty.of(VkStagingImage.class, PROPERTY_NAME_TEXTURE_NORMAL);

  public final static MaterialProperty<Float> FLOAT_SHININESS = MaterialProperty.of(Float.class, PROPERTY_NAME_FLOAT_SHININESS);

  static {
    properties.put(PROPERTY_NAME_COLOR_AMBIENT,   COLOR_AMBIENT);
    properties.put(PROPERTY_NAME_COLOR_DIFFUSE,   COLOR_DIFFUSE);
    properties.put(PROPERTY_NAME_COLOR_SPECULAR,  COLOR_SPECULAR);
    properties.put(PROPERTY_NAME_COLOR_EMISSIVE,  COLOR_EMISSIVE);
    properties.put(PROPERTY_NAME_TEXTURE_ALBEDO,  TEXTURE_ALBEDO);
    properties.put(PROPERTY_NAME_TEXTURE_NORMAL,  TEXTURE_NORMAL);
    properties.put(PROPERTY_NAME_FLOAT_SHININESS, FLOAT_SHININESS);
  }

  public static <T> MaterialProperty<T> get(String name) {
    return (MaterialProperty<T>) properties.get(name);
  }

}
