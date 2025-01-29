package org.sc.themis.renderer.material;

import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.scene.Model;
import org.sc.themis.shared.exception.ThemisException;

public class MaterialManager {

    private final Material defaultMaterial;
    private Material lastUsedMaterial = null;

    public MaterialManager( Material defaultMaterial ) {
        this.defaultMaterial = defaultMaterial;
    }

    public void compile( MaterialProperties ... properties ) throws ThemisException {

        for ( MaterialProperties mProperties : properties ) {

            String variantIdentifier = this.defaultMaterial.add( mProperties );

            if ( variantIdentifier != null ) {
                mProperties.setVariantIdentifier(this.defaultMaterial, variantIdentifier);
            }

        }
    }

    public void bindMaterial(VkCommand command, Model model) throws ThemisException {

        Material material = select( model );

        if ( this.lastUsedMaterial == null || !this.lastUsedMaterial.equals(material) ) {
            this.lastUsedMaterial = material;
        }

        command.bindPipeline( this.lastUsedMaterial.getPipeline() );

    }

    public void bindMaterialVariant( VkCommand command, MaterialProperties properties, int frame) throws ThemisException {
        int[] indexedOffest = new int[0];
        VkDescriptorSet [] descriptorsets = this.lastUsedMaterial.getDescriptorSets(frame, properties);
        command.bindDescriptorSets( indexedOffest, descriptorsets );
    }

    public boolean isValid( MaterialProperties properties ) {
        return properties.getVariantIdentifier( this.lastUsedMaterial ) != null;
    }

    private Material select(Model model) {
        return this.defaultMaterial;
    }

    public MaterialProperties select( MaterialProperties ... properties ) {

        for ( MaterialProperties materialProperties : properties ) {
            if ( materialProperties.getVariantIdentifier( this.lastUsedMaterial ) != null ) {
                return materialProperties;
            }
        }

        return null;

    }
}
