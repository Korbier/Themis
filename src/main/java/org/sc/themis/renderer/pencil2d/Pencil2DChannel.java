package org.sc.themis.renderer.pencil2d;

import org.sc.themis.shared.utils.MemorySizeUtils;

public class Pencil2DChannel {

  private Pencil2DVertex[] vertices = new Pencil2DVertex[0];
  private int [] indices = new int[0];

  public void clear() {
    this.vertices = new Pencil2DVertex[0];
    this.indices = new int[0];
  }

  public void append(Pencil2DVertex [] inputVertices, int [] inputIndices) {

    int offset = this.vertices.length;

    Pencil2DVertex[] vData = new Pencil2DVertex[this.vertices.length + inputVertices.length];
    System.arraycopy(this.vertices, 0, vData, 0, this.vertices.length);
    System.arraycopy(inputVertices, 0, vData, this.vertices.length, inputVertices.length);
    this.vertices = vData;

    for (int i=0; i<inputIndices.length; i++) {
      inputIndices[i] = inputIndices[i] + offset;
    }

    int[] iData = new int[this.indices.length + inputIndices.length];
    System.arraycopy(this.indices, 0, iData, 0, this.indices.length);
    System.arraycopy(inputIndices, 0, iData, this.indices.length, inputIndices.length);
    this.indices = iData;

  }

  public float[] getVertices() {

    float [] data = new float[getVertexLength()];
    int offset = 0;

    for (Pencil2DVertex v : this.vertices) {
      data[offset++] = v.position().x();
      data[offset++] = v.position().y();
      data[offset++] = v.texture().x();
      data[offset++] = v.texture().y();
      data[offset++] = v.color().x();
      data[offset++] = v.color().y();
      data[offset++] = v.color().z();
      data[offset++] = v.color().w();
      data[offset++] = v.properties().x();
      data[offset++] = v.properties().y();
      data[offset++] = v.properties().z();
      data[offset++] = v.properties().w();
    }

    return data;

  }

  public int getVertexLength() {
    return this.vertices.length * Pencil2DVertex.SIZE;
  }

  public int[] getIndices() {
    return this.indices;
  }

  public int getIndiceCount() {
    return this.indices.length;
  }

  public boolean isRenderable() {
    return getIndiceCount() > 0;
  }

}
