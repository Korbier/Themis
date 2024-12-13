package org.sc.themis.scene;

import org.joml.Matrix4f;
import org.sc.themis.shared.Configuration;

public class Projection {

    private final Configuration configuration;
    private final Matrix4f perspective;
    private final Matrix4f orthographic;

    public Projection(Configuration configuration) {
        this.configuration = configuration;
        this.perspective  = new Matrix4f();
        this.orthographic = new Matrix4f();
    }

    public Matrix4f perspective() {
        return perspective;
    }

    public Matrix4f orthographic() {
        return this.orthographic;
    }

    public void resize(int width, int height) {

        float fov   = this.configuration.scene().projection().fov();
        float znear = this.configuration.scene().projection().znear();
        float zfar  = this.configuration.scene().projection().zfar();

        perspective().identity();
        perspective().perspective(
            (float) Math.toRadians( fov ), (float) width / (float) height,
            znear, zfar, true
        );

        orthographic().identity();
        orthographic().ortho( 0, (float) width, (float) height, 0, znear, zfar, true);

    }

}
