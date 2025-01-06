package org.sc.themis.scene.material;

import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.scene.MeshPropertiesMap;
import org.sc.themis.shared.exception.ThemisException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MaterialAllocator {

    private Map<String, BaseMaterial> materials = new HashMap<>();

    public void add( BaseMaterial material ) {
        this.materials.put( material.getIdentifier(), material );
    }

    public void cleanup() throws ThemisException {
        for ( BaseMaterial material : materials() ) {
            material.cleanup();
        }
    }

    public void allocate(MeshPropertiesMap properties) throws ThemisException {
        for ( BaseMaterial material : materials() ) {
            String variantIdentifier = material.add( properties );
            properties.setMaterialVariantIdentifier( material, variantIdentifier );
        }
    }

    public Collection<BaseMaterial> materials() {
        return this.materials.values();
    }

    public VkDescriptorSet[] getDescriptorSets( BaseMaterial material, MeshPropertiesMap props, int frame ) {
        return this.materials.get( material.getIdentifier() ).getDescriptorSet( props, frame );
    }

}
