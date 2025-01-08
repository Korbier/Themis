package org.sc.themis.scene.material;

import org.joml.Vector4f;
import org.sc.themis.renderer.resource.staging.VkStagingImage;

public interface MaterialProperty<D> {

    MaterialProperty<Vector4f> COLOR_BASE      = MaterialProperty.of( Vector4f.class );
    MaterialProperty<Vector4f> COLOR_DIFFUSE   = MaterialProperty.of( Vector4f.class );
    MaterialProperty<Vector4f> COLOR_EMISSIVE  = MaterialProperty.of( Vector4f.class );
    MaterialProperty<Vector4f> COLOR_SPECULAR  = MaterialProperty.of( Vector4f.class );
    MaterialProperty<Float>    COLOR_SHININESS = MaterialProperty.of( Float.class );

    MaterialProperty<VkStagingImage> TEXTURE_BASE = MaterialProperty.of( VkStagingImage.class );


    static <T> MaterialProperty<T> of(Class<T> clazz ) {
        return () -> clazz;
    }

    Class<D> getType();

}
