package org.sc.themis.renderer.material;

import org.joml.Vector4f;
import org.sc.themis.renderer.resource.VkStagingImage;

/**
 * Material properties.
 *
 * @param <D> property type
 */
public interface MaterialProperty<D> {

  /** Color properties. */
  interface Color {
    MaterialProperty<Vector4f> BASE = MaterialProperty.of(Vector4f.class, "color.base");
    MaterialProperty<Vector4f> DIFFUSE = MaterialProperty.of(Vector4f.class, "color.diffuse");
    MaterialProperty<Vector4f> SPECULAR = MaterialProperty.of(Vector4f.class, "color.specular");
    MaterialProperty<Vector4f> EMISSIVE = MaterialProperty.of(Vector4f.class, "color.emissive");
  }

  /** Texture properties. */
  interface Texture {
    MaterialProperty<VkStagingImage> BASE = MaterialProperty.of(VkStagingImage.class, "texture.base");
    MaterialProperty<VkStagingImage> NORMALS = MaterialProperty.of(VkStagingImage.class, "texture.normal");
  }

  /** Other properties. */
  interface Property {
    MaterialProperty<Float> SHININESS = MaterialProperty.of(Float.class, "property.shininess");
  }

  /**
   * Property factory helper.
   *
   * @param clazz Property class (=type)
   * @param name Property name
   * @return Property of given type
   */
  static <T> MaterialProperty<T> of(Class<T> clazz, String name) {
    return new MaterialProperty<>() {

      @Override
      public Class<T> getType() {
        return clazz;
      }

      @Override
      public String getName() {
        return name;
      }
    };
  }

  /** Property type. */
  Class<D> getType();

  /** Property name. */
  String getName();
}
