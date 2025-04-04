package org.sc.themis.renderer.pencil2d;

import org.sc.themis.renderer.resource.font.FontRepository;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class Pencil2D {

  private Map<Integer,Pencil2DLayer> layers = new TreeMap<>();
  private FontRepository fonts = null;

  public Pencil2D() {
    this(null);
  }

  public Pencil2D(FontRepository fonts) {
    this.fonts = fonts;
  }

  public FontRepository fonts() {
    return fonts;
  }

  public void clear() {
    for (Pencil2DLayer layer : this.layers.values()) {
      layer.clear();
    }
  }

  public Pencil2DLayer layer(int zindex) {
    Pencil2DLayer layer = new Pencil2DLayer(this);
    this.layers.put(zindex, layer);
    return layer;
  }

  public Collection<Pencil2DLayer> layers() {
    return this.layers.values();
  }

}
