package org.sc.themis.renderer.material;

import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.shared.exception.ThemisException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MaterialAllocator {

    private Map<String, Material> materials = new HashMap<>();

    public void add( Material material ) {
        this.materials.put( material.getIdentifier(), material );
    }

    public void cleanup() throws ThemisException {
        for ( Material material : materials() ) {
            material.cleanup();
        }
    }

    public void allocate(MaterialProperties properties) throws ThemisException {
        for ( Material material : materials() ) {
            String variantIdentifier = material.add( properties );
            properties.setVariantIdentifier( material, variantIdentifier );
        }
    }

    public Collection<Material> materials() {
        return this.materials.values();
    }

    public VkDescriptorSet[] getDescriptorSets(Material material, MaterialProperties props, int frame ) {
        return this.materials.get( material.getIdentifier() ).getDescriptorSets( frame, props );
    }

}
