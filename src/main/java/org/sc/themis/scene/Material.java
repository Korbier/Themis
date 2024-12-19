package org.sc.themis.scene;

import org.joml.Vector4f;
import org.sc.themis.renderer.resource.staging.VkStagingImage;
import org.sc.themis.renderer.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.Image;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.vulkan.VK10.*;

public class Material {

    private final String identifier;
    private final VkStagingResourceAllocator resourceAllocator;

    private final Map<MaterialAttribute, Vector4f> colors   = new HashMap<>();
    private final Map<MaterialAttribute, VkStagingImage> textures = new HashMap<>();

    private boolean renderable = false;

    public Material(VkStagingResourceAllocator resourceAllocator, String identifier ) {
        this.identifier = identifier;
        this.resourceAllocator = resourceAllocator;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void cleanup() throws ThemisException {
        this.renderable = false;
        for ( VkStagingImage texture : this.textures.values() ) {
            texture.cleanup();
        }
    }

    public Material setColor(MaterialAttribute attribute, Vector4f color ) {
        this.colors.put( attribute, color );
        return this;
    }

    public Vector4f getColor( MaterialAttribute attribute ) {
        return this.colors.get( attribute );
    }

    public Material setTexture(MaterialAttribute attribute, Image image) throws ThemisException {
        VkStagingImage vkImage = this.resourceAllocator.allocateImage(VK_FORMAT_R8G8B8A8_SRGB);
        vkImage.load( image );
        this.textures.put( attribute, vkImage );
        return this;
    }

    public VkStagingImage getTexture( MaterialAttribute attribute ) {
        return this.textures.get( attribute );
    }

    public boolean isRenderable() {

        if ( this.renderable ) {
            return true;
        }

        for ( VkStagingImage texture : this.textures.values() ) {
            if ( !texture.isRenderable() ) return false;
        }

        this.renderable = true;

        return true;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return Objects.equals(identifier, material.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
