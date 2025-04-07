package org.sc.themis.renderer.pencil2d;

import org.joml.Vector4f;
import org.sc.themis.renderer.resource.font.Font;
import org.sc.themis.renderer.resource.font.FontCharacter;

public class Pencil2DLayer {

  private final Pencil2D parent;

  private final Pencil2DChannel triangleChannel = new Pencil2DChannel();
  private final Pencil2DChannel lineChannel = new Pencil2DChannel();
  private final Pencil2DChannel textChannel = new Pencil2DChannel();

  private final Color defaultColor = Color.of("ffffff");
  private final int defaultFont = -1;

  private Color color = defaultColor;
  private int fontIdx = -1;

  public Pencil2DLayer(Pencil2D parent) {
    this.parent = parent;
  }

  public boolean isRenderable() {
    return this.triangleChannel.isRenderable() && this.lineChannel.isRenderable();
  }

  public Pencil2DChannel getTriangleChannel() {
    return this.triangleChannel;
  }

  public Pencil2DChannel getLineChannel() {
    return this.lineChannel;
  }

  public Pencil2DChannel getTextChannel() {
    return this.textChannel;
  }

  public Pencil2DLayer reset() {
    return color(this.defaultColor);
  }

  public Pencil2DLayer color(Color color) {
    this.color = color;
    return this;
  }

  public Pencil2DLayer font(int fontIdx) {
    this.fontIdx = fontIdx;
    return this;
  }

  public Pencil2DLayer rect(int x, int y, int width, int height) {

    Pencil2DVertex a = new Pencil2DVertex(x,         y,          color.r(), color.g(), color.b(), 1.0f);
    Pencil2DVertex b = new Pencil2DVertex(x + width, y,          color.r(), color.g(), color.b(), 1.0f);
    Pencil2DVertex c = new Pencil2DVertex(x + width, y + height, color.r(), color.g(), color.b(), 1.0f);
    Pencil2DVertex d = new Pencil2DVertex(x,         y + height, color.r(), color.g(), color.b(), 1.0f);

    this.triangleChannel.append(new Pencil2DVertex[] {a, b, c, d}, new int[] {0, 1, 3, 1, 2, 3});

    return this;

  }

  public Pencil2DLayer text(int x, int y, String text) {

    if (this.fontIdx < 0 || !isFontRepositoryAvailable()) {
      //todo put a warning here
      return this;
    }

    Font font = this.parent.fonts().get(this.fontIdx);

    FontCharacter[] characters = font.toCharacters(text);

    float decal = 0;

    for (FontCharacter fchar : characters) {

      float posX = x + decal + fchar.bearing().x();
      float posY = y + font.getMaxYBearing() - fchar.bearing().y();

      character(
          posX, posY, fchar.size().x(), fchar.size().y(),
          fchar.uv0().x, fchar.uv0().y, fchar.uv1().x, fchar.uv1().y,
          fontIdx, font.isSdfFont() ? font.getSdfWidth() : 0.0f, font.isSdfFont() ? font.getSdfEdge() : 0.0f
      );

      decal += fchar.advance();

    }
    return this;

  }

  private void character(float x, float y, float w, float h, float uMin, float vMin, float uMax, float vMax, float fontIdx, float sdfW, float sdfE) {

    Pencil2DVertex a = new Pencil2DVertex(x,     y,     uMin, vMin, color.r(), color.g(), color.b(), 1.0f, fontIdx, sdfW, sdfE, 1.0f);
    Pencil2DVertex b = new Pencil2DVertex(x + w, y,     uMax, vMin, color.r(), color.g(), color.b(), 1.0f, fontIdx, sdfW, sdfE, 1.0f);
    Pencil2DVertex c = new Pencil2DVertex(x + w, y + h, uMax, vMax, color.r(), color.g(), color.b(), 1.0f, fontIdx, sdfW, sdfE, 1.0f);
    Pencil2DVertex d = new Pencil2DVertex(x,     y + h, uMin, vMax, color.r(), color.g(), color.b(), 1.0f, fontIdx, sdfW, sdfE, 1.0f);

    this.triangleChannel.append(new Pencil2DVertex[] {a, b, c, d}, new int[] {0, 1, 3, 1, 2, 3});

  }

  public void clear() {
    this.triangleChannel.clear();
    this.textChannel.clear();
    this.lineChannel.clear();
  }

  private boolean isFontRepositoryAvailable() {
    return this.parent.fonts() != null && this.parent.fonts().size() > 0;
  }

}
