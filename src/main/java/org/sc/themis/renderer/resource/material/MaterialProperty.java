package org.sc.themis.renderer.resource.material;

import org.joml.Vector4f;
import org.sc.themis.renderer.base.resource.staging.VkStagingImage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Material properties.
 *
 * @param <D> property type
 */
public interface MaterialProperty<D> {

  /** Property type. */
  Class<D> getType();

  /** Property name. */
  String getName();

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

      @Override
      public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return Objects.equals(getName(), ((MaterialProperty<?>) obj).getName() );
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(getName());
      }

      public String toString() {
        return getName() + "(" + getType().getCanonicalName() + ")";
      }

    };
  }

}
