package org.sc.themis.renderer.resource.material;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Material {

  private final String name;
  private final String author;
  private final String source;
  private final Map<MaterialProperty<?>, Object> properties = new HashMap<>();

  public Material(String name) {
    this(name, "no-author", "no-source");
  }

  public Material(String name, String author, String source) {
    this.name = name;
    this.author = author;
    this.source = source;
  }

  public String getName() {
    return name;
  }

  public String getAuthor() {
    return author;
  }

  public String getSource() {
    return source;
  }

  public <T> T get(MaterialProperty<T> property) {
    return (T) this.properties.get(property);
  }

  public <T> void set(MaterialProperty<T> property, T value) {
    this.properties.put(property, value);
  }

  public boolean containsKeys(MaterialProperty<?>... properties) {

    for (MaterialProperty<?> property : properties) {
      if (!this.properties.containsKey(property)) {
        return false;
      }
    }

    return true;

  }

  public Collection<Object> values() {
    return this.properties.values();
  }

}
