package org.sc.themis.renderer.pencil2d;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.sc.themis.shared.utils.MemorySizeUtils;

public record Pencil2DVertex(
    Vector2f position,
    Vector2f texture,
    Vector4f color,
    Vector4f properties
) {

  public final static int SIZE = MemorySizeUtils.VEC2F * 2 + MemorySizeUtils.VEC4F * 2;

  public Pencil2DVertex(
    float x, float y,
    float u, float v,
    float r, float g, float b, float a,
    float p0, float p1, float p2, float p3
  ) {
    this(new Vector2f(x, y), new Vector2f(u, v), new Vector4f(r, g, b, a), new Vector4f(p0, p1, p2, p3));
  }

  public Pencil2DVertex( float x, float y, float r, float g, float b, float a ) {
    this(new Vector2f(x, y), new Vector2f(), new Vector4f(r, g, b, a), new Vector4f());
  }
}
