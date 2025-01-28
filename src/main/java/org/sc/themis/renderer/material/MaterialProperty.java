package org.sc.themis.renderer.material;

import org.joml.Vector4f;
import org.sc.themis.renderer.resource.staging.VkStagingImage;

public interface MaterialProperty<D> {


    interface Color {
        MaterialProperty<Vector4f> BASE      = MaterialProperty.of( Vector4f.class );
        MaterialProperty<Vector4f> DIFFUSE   = MaterialProperty.of( Vector4f.class );
        MaterialProperty<Vector4f> EMISSIVE  = MaterialProperty.of( Vector4f.class );
        MaterialProperty<Vector4f> SPECULAR  = MaterialProperty.of( Vector4f.class );
    }

    interface Texture {
        MaterialProperty<VkStagingImage> BASE = MaterialProperty.of( VkStagingImage.class );
    }

    interface Property {
        MaterialProperty<Float>    SHININESS = MaterialProperty.of( Float.class );
    }

    static <T> MaterialProperty<T> of(Class<T> clazz ) {
        return () -> clazz;
    }

    Class<D> getType();

}
