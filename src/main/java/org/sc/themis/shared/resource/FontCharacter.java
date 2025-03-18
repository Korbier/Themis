package org.sc.themis.shared.resource;

import org.joml.Vector2i;

public record FontCharacter(
    char symbol,
    Image image,
    Vector2i size,
    Vector2i bearing,
    long advance
) {
}
