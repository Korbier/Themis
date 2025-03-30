package org.sc.themis.renderer.resource.material;

import org.sc.themis.renderer.material.MaterialRenderer;

import java.util.HashMap;
import java.util.Objects;

public class Material extends HashMap<MaterialProperty<?>, Object> {

  private final HashMap<String, String> variantIdentifier = new HashMap<>();

  public <T> T getProperty(MaterialProperty<T> property) {
    return (T) get(property);
  }

  public void setVariantIdentifier(MaterialRenderer materialRenderer, String identifier) {
    this.variantIdentifier.put(materialRenderer.getIdentifier(), identifier);
  }

  public String getVariantIdentifier(MaterialRenderer materialRenderer) {
    return this.variantIdentifier.get(materialRenderer.getIdentifier());
  }

  public boolean containsKeys(MaterialProperty<?>... properties) {

    for (MaterialProperty<?> property : properties) {
      if (!containsKey(property)) {
        return false;
      }
    }

    return true;
  }

  public String generateVariantIdentifier(MaterialProperty<?>... properties) {
    return Integer.toString(Objects.hashCode(properties));
  }
}
