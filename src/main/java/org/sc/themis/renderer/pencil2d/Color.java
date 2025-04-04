package org.sc.themis.renderer.pencil2d;

public class Color {

  private final int hexaRed;
  private final int hexaGreen;
  private final int hexaBlue;

  private final float red;
  private final float green;
  private final float blue;

  public static Color of(String cssColor) {

    StringBuilder color = new StringBuilder(cssColor.replace("#", "").substring(0, 6));

    while (color.length() < 6) {
      color.append("0");
    }

    String sColor = color.toString();

    return new Color(
      sColor.substring(0, 2),
      sColor.substring(2, 4),
      sColor.substring(4, 6)
    );

  }

  private Color(String hexaRed, String hexaGreen, String hexaBlue) {
    this.hexaRed = Integer.parseInt(hexaRed, 16);
    this.hexaGreen = Integer.parseInt(hexaGreen, 16);
    this.hexaBlue = Integer.parseInt(hexaBlue, 16);
    this.red = (float) this.hexaRed / 255;
    this.green = (float) this.hexaGreen / 255;
    this.blue = (float) this.hexaBlue / 255;
  }

  public String getCssColor() {
    return "%s%s%s".formatted(r(), g(), b());
  }

  public int hr() {
    return this.hexaRed;
  }

  public float hg() {
    return this.hexaGreen;
  }

  public float hb() {
    return this.hexaBlue;
  }

  public float r() {
    return this.red;
  }

  public float g() {
    return this.green;
  }

  public float b() {
    return this.blue;
  }

  @Override
  public String toString() {
    return "Color{hexaRed=%d, hexaGreen=%d, hexaBlue=%d, red=%s, green=%s, blue=%s}"
      .formatted(hexaRed, hexaGreen, hexaBlue, red, green, blue);
  }
}
