package org.sc.viewer.renderactivity.ui.draw;

import org.joml.Vector2f;

public record DrawVertex(Vector2f position, Vector2f texture) {

    public static final int SIZE = 2 + 2;

    public static DrawVertex of(float x, float y) {
        return of(x, y, 0.0f, 0.0f);
    }

    public static DrawVertex of(float x, float y, float u, float v) {
        return new DrawVertex(new Vector2f(x, y), new Vector2f(u, v));
    }

}
