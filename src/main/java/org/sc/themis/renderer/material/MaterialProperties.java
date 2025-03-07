package org.sc.themis.renderer.material;

import java.util.HashMap;
import java.util.Objects;

public class MaterialProperties extends HashMap<MaterialProperty<?>, Object> {

  private final HashMap<String, String> variantIdentifier = new HashMap<>();

  public <T> T getProperty(MaterialProperty<T> property) {
    return (T) get(property);
  }

  public void setVariantIdentifier(Material material, String identifier) {
    this.variantIdentifier.put(material.getIdentifier(), identifier);
  }

  public String getVariantIdentifier(Material material) {
    return this.variantIdentifier.get(material.getIdentifier());
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
