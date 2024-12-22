package org.sc.themis.scene;

import org.joml.Vector4f;

public class MeshProperties {

    public final static MeshProperty<Vector4f> COLOR_BASE      = MeshProperty.of( Vector4f.class );
    public final static MeshProperty<Vector4f> COLOR_DIFFUSE   = MeshProperty.of( Vector4f.class );
    public final static MeshProperty<Vector4f> COLOR_EMISSIVE  = MeshProperty.of( Vector4f.class );
    public final static MeshProperty<Vector4f> COLOR_SPECULAR  = MeshProperty.of( Vector4f.class );
    public final static MeshProperty<Float>    COLOR_SHININESS = MeshProperty.of( Float.class );

    private MeshProperties() {}

}
