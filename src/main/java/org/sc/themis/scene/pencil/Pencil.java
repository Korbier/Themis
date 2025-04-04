package org.sc.themis.scene.pencil;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.sc.themis.renderer.pencil2d.Color;
import org.sc.themis.renderer.resource.font.Font;
import org.sc.themis.renderer.resource.font.FontCharacter;
import org.sc.themis.renderer.resource.font.FontRepository;

public class Pencil {

  private static final int COMPONENT_COUNT = 11;

  private final FontRepository fontRepository;

  private float[] data = new float[0];
  private int[] indices = new int[0];

  public Pencil(FontRepository repository) {
    this.fontRepository = repository;
  }

  public FontRepository getFontRepository() {
    return this.fontRepository;
  }

  public int getDataSize() {
    return this.data.length;
  }

  public float[] getData() {
    return this.data;
  }

  public int getIndiceSize() {
    return this.indices.length;
  }

  public int[] getIndices() {
    return this.indices;
  }

  public boolean isRenderable() {
    return this.data.length > 0 && this.indices.length > 0;
  }

  public Pencil clear() {
    this.data = new float[0];
    this.indices = new int[0];
    return this;
  }

  public Pencil text(Vector2f position, int fontIdx, Color color, String text) {

    if (this.fontRepository == null || !this.fontRepository.contains(fontIdx)) {
      System.err.println("[Use a logger here] font not found");
      return this;
    }

    Font font = this.fontRepository.get(fontIdx);
    FontCharacter [] characters = font.toCharacters(text);

    float decal = 0;

    for (FontCharacter fchar : characters) {

      float posX = position.x() + decal + fchar.bearing().x();
      float posY = position.y() + font.getMaxYBearing() - fchar.bearing().y();

      Vector3f fontProperties = new Vector3f(
          fontIdx,
          font.isSdfFont() ? font.getSdfWidth() : 0.0f,
          font.isSdfFont() ? font.getSdfEdge() : 0.0f
        );

      appendChar(
          new Vector2f(posX, posY), new Vector2f(fchar.size().x(), fchar.size().y()),
          fchar.uv0(), fchar.uv1(),
          color, fontProperties
      );

      decal += fchar.advance();

    }
    return this;

  }

  public Pencil triangle(Vector2f a, Vector2f b, Vector2f c, Color color) {

    int startIndiceOffset = getDataSize() / COMPONENT_COUNT;

    float[] data = extendData(3);
    appendData(data, getDataSize(), a, color);
    appendData(data, getDataSize() + COMPONENT_COUNT, b, color);
    appendData(data, getDataSize() + COMPONENT_COUNT * 2, c, color);
    this.data = data;

    int[] indices = extendIndices(3);
    setIndice(indices, getIndiceSize(), startIndiceOffset);
    setIndice(indices, getIndiceSize() + 1, startIndiceOffset + 1);
    setIndice(indices, getIndiceSize() + 2, startIndiceOffset + 2);
    this.indices = indices;

    return this;
  }

  public Pencil rect(Vector2f position, Vector2f size, Color color) {

    int startIndiceOffset = getDataSize() / COMPONENT_COUNT;

    float[] data = extendData(4);

    appendData(
        data, getDataSize(),
        position,
        color
    );
    appendData(
        data, getDataSize() + COMPONENT_COUNT,
        new Vector2f(position.x() + size.x(), position.y()),
        color
    );
    appendData(
        data, getDataSize() + COMPONENT_COUNT * 2,
        new Vector2f(position.x() + size.x(), position.y() + size.y()),
        color
    );
    appendData(
        data, getDataSize() + COMPONENT_COUNT * 3,
        new Vector2f(position.x(), position.y() + size.y()),
        color
    );
    this.data = data;

    int[] indices = extendIndices(6);
    setIndice(indices, getIndiceSize(), startIndiceOffset);
    setIndice(indices, getIndiceSize() + 1, startIndiceOffset + 1);
    setIndice(indices, getIndiceSize() + 2, startIndiceOffset + 3);
    setIndice(indices, getIndiceSize() + 3, startIndiceOffset + 1);
    setIndice(indices, getIndiceSize() + 4, startIndiceOffset + 2);
    setIndice(indices, getIndiceSize() + 5, startIndiceOffset + 3);
    this.indices = indices;

    return this;

  }

  private Pencil appendChar(
      Vector2f position, Vector2f size,
      Vector2f textureMin, Vector2f textureMax,
      Color color, Vector3f fontProperties
  ) {

    int startIndiceOffset = getDataSize() / COMPONENT_COUNT;

    float[] data = extendData(4);
    appendData(
        data, getDataSize(),
        position, textureMin,
        color, new Vector4f(1.0f, fontProperties.x(), fontProperties.y(), fontProperties.z())
    );
    appendData(
        data, getDataSize() + COMPONENT_COUNT,
        new Vector2f(position.x() + size.x(), position.y()),
        new Vector2f(textureMax.x, textureMin.y),
        color, new Vector4f(1.0f, fontProperties.x(), fontProperties.y(), fontProperties.z())
    );
    appendData(
        data, getDataSize() + COMPONENT_COUNT * 2,
        new Vector2f(position.x() + size.x(), position.y() + size.y()),
        textureMax,
        color, new Vector4f(1.0f, fontProperties.x(), fontProperties.y(), fontProperties.z())
    );
    appendData(
        data, getDataSize() + COMPONENT_COUNT * 3,
        new Vector2f(position.x(), position.y() + size.y()),
        new Vector2f(textureMin.x, textureMax.y),
        color, new Vector4f(1.0f, fontProperties.x(), fontProperties.y(), fontProperties.z())
    );
    this.data = data;

    int[] indices = extendIndices(6);
    setIndice(indices, getIndiceSize(), startIndiceOffset);
    setIndice(indices, getIndiceSize() + 1, startIndiceOffset + 1);
    setIndice(indices, getIndiceSize() + 2, startIndiceOffset + 3);
    setIndice(indices, getIndiceSize() + 3, startIndiceOffset + 1);
    setIndice(indices, getIndiceSize() + 4, startIndiceOffset + 2);
    setIndice(indices, getIndiceSize() + 5, startIndiceOffset + 3);
    this.indices = indices;

    return this;
  }

  private float[] extendData(int additionalSize) {
    float[] data = new float[this.data.length + additionalSize * COMPONENT_COUNT];
    System.arraycopy(this.data, 0, data, 0, this.data.length);
    return data;
  }

  private int[] extendIndices(int additionalSize) {
    int[] indices = new int[this.indices.length + additionalSize];
    System.arraycopy(this.indices, 0, indices, 0, this.indices.length);
    return indices;
  }

  private void appendData(float[] data, int idx, Vector2f position, Color color) {
    appendData(data, idx, position, new Vector2f(), color, new Vector4f());
  }

  private void appendData(
      float[] data, int idx,
      Vector2f position, Vector2f texture, Color color,
      Vector4f properties
  ) {

    data[idx] = position.x();
    data[idx + 1] = position.y();

    data[idx + 2] = texture.x();
    data[idx + 3] = texture.y();

    data[idx + 4] = color.r();
    data[idx + 5] = color.g();
    data[idx + 6] = color.b();

    data[idx + 7] = properties.x();
    data[idx + 8] = properties.y();
    data[idx + 9] = properties.z();
    data[idx + 10] = properties.w();

  }

  private void setIndice(int[] indices, int idx, int value) {
    indices[idx] = value;
  }
}
