package org.sc.themis.renderer.resource.font;

import org.joml.Vector2f;
import org.joml.Vector2i;

public record FontCharacter(
    char symbol,
    Vector2i size,
    Vector2i bearing,
    Vector2f uv0, Vector2f uv1,
    long advance
) {
}
