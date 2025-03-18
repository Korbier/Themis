package org.sc.themis.shared.resource.old;

public record FontCharacter(
    char character, float advance,
    float width, float height,
    float u0, float v0, float u1, float v1,
    float xOffset, float yOffset
) {
}