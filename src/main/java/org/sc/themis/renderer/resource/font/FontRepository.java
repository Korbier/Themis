package org.sc.themis.renderer.resource.font;

import org.sc.themis.renderer.resource.image.Image;

import java.util.ArrayList;
import java.util.List;

public class FontRepository {

  private final List<Font> fonts = new ArrayList<>();

  public int load(Font font) {
    int idx = this.fonts.size();
    this.fonts.add(font);
    return idx;
  }

  public Font get(int idx) {
    return this.fonts.get(idx);
  }

  public boolean contains(int idx) {
    return this.fonts.size() > idx;
  }

  public int size() {
    return this.fonts.size();
  }

  public Image[] getTextures() {
    return this.fonts.stream().map(Font::getTexture).toArray(Image[]::new);
  }

}
