package org.sc.themis.renderer.resource.material;


import org.joml.Vector4f;
import org.sc.themis.renderer.base.resource.staging.VkStagingImage;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;

public class MaterialProperties {

  public static Map<String, MaterialProperty<?>> properties = new HashMap<>();

  public final static String PROPERTY_NAME_COLOR_AMBIENT  = "color.ambient";
  public final static String PROPERTY_NAME_COLOR_DIFFUSE  = "color.diffuse";
  public final static String PROPERTY_NAME_COLOR_SPECULAR = "color.specular";
  public final static String PROPERTY_NAME_COLOR_EMISSIVE = "color.emissive";

  public final static String PROPERTY_NAME_TEXTURE_ALBEDO = "texture.albedo";
  public final static String PROPERTY_NAME_TEXTURE_NORMAL = "texture.normal";
  public final static String PROPERTY_NAME_FLOAT_SHININESS = "float.shininess";

  public final static MaterialProperty<Vector4f> COLOR_AMBIENT = MaterialProperty.of(Vector4f.class, PROPERTY_NAME_COLOR_AMBIENT, -1);
  public final static MaterialProperty<Vector4f> COLOR_DIFFUSE = MaterialProperty.of(Vector4f.class, PROPERTY_NAME_COLOR_DIFFUSE, -1);
  public final static MaterialProperty<Vector4f> COLOR_SPECULAR = MaterialProperty.of(Vector4f.class, PROPERTY_NAME_COLOR_SPECULAR, -1);
  public final static MaterialProperty<Vector4f> COLOR_EMISSIVE = MaterialProperty.of(Vector4f.class, PROPERTY_NAME_COLOR_EMISSIVE, -1);

  public final static MaterialProperty<VkStagingImage> TEXTURE_ALBEDO = MaterialProperty.of(VkStagingImage.class, PROPERTY_NAME_TEXTURE_ALBEDO, VK_FORMAT_R8G8B8A8_SRGB);
  public final static MaterialProperty<VkStagingImage> TEXTURE_NORMAL = MaterialProperty.of(VkStagingImage.class, PROPERTY_NAME_TEXTURE_NORMAL, VK_FORMAT_R8G8B8A8_UNORM);

  public final static MaterialProperty<Float> FLOAT_SHININESS = MaterialProperty.of(Float.class, PROPERTY_NAME_FLOAT_SHININESS, -1);

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
