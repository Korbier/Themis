package org.sc.themis.renderer.resource.font;

import jakarta.enterprise.context.ApplicationScoped;
import org.sc.themis.renderer.resource.image.Image;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.service.Service;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FontRepository implements Service {

  private final List<Font> fonts = new ArrayList<>();

  @Override
  public void setup() throws ThemisException {}

  @Override
  public void cleanup() throws ThemisException {}

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
