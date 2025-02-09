package org.sc.themis.renderer.material;

import org.joml.Vector4f;
import org.sc.themis.renderer.resource.staging.VkStagingImage;

public interface MaterialProperty<D> {

    interface Color {
        MaterialProperty<Vector4f> BASE     = MaterialProperty.of(Vector4f.class, "color.base");
        MaterialProperty<Vector4f> DIFFUSE  = MaterialProperty.of(Vector4f.class, "color.diffuse");
        MaterialProperty<Vector4f> SPECULAR = MaterialProperty.of(Vector4f.class, "color.specular");
        MaterialProperty<Vector4f> EMISSIVE = MaterialProperty.of(Vector4f.class, "color.emissive");
    }

    interface Texture {
        MaterialProperty<VkStagingImage> BASE = MaterialProperty.of(VkStagingImage.class, "texture.base");
        MaterialProperty<VkStagingImage> NORMALS = MaterialProperty.of(VkStagingImage.class, "texture.normal");
    }

    interface Property {
        MaterialProperty<Float> SHININESS = MaterialProperty.of(Float.class, "property.shininess");
    }

    static <T> MaterialProperty<T> of(Class<T> clazz, String name) {
        return new MaterialProperty<T>() {

            @Override
            public Class<T> getType() {
                return clazz;
            }

            @Override
            public String getName() {
                return name;
            }

        };
    }

    Class<D> getType();
    String getName();

}
