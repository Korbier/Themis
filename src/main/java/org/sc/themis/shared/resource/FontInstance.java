package org.sc.themis.shared.resource;

public enum FontInstance {

  VERDANA_12("verdana_df", 12, 0.40f, 0.24f),
  VERDANA_14("verdana_df", 14, 0.38f, 0.26f)
  ;

  private String fontname;
  private int size;
  private float width;
  private float edge;

  private Font font;

  private FontInstance(String fontname, int size, float width, float edge) {
    this.fontname = fontname;
    this.size = size;
    this.width = width;
    this.edge = edge;
  }

  public String fontname() {
    return fontname;
  }

  public int size() {
    return size;
  }

  public float width() {
    return width;
  }

  public float edge() {
    return edge;
  }

  public Font font() {

    if (this.font == null) {
      this.font = Font.of("src/main/resources/font/%s.fnt".formatted(fontname()));
    }

    return this.font;

  }

}
