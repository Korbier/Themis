package org.sc.themis.scene;

import org.sc.themis.scene.material.BaseMaterial;

import java.util.HashMap;

public class MeshPropertiesMap extends HashMap<MeshProperty<?>, Object> {

    private final HashMap<String, String> materialVariantIdentifier = new HashMap<>();

    public <T> T getProperty( MeshProperty<T> property ) {
        return (T) get( property );
    }

    public void setMaterialVariantIdentifier(BaseMaterial material, String identifier) {
        this.materialVariantIdentifier.put( material.getIdentifier(), identifier );
    }

    public String getMaterialVariantIdentifier( BaseMaterial material ) {
        return this.materialVariantIdentifier.get( material.getIdentifier() );
    }

}
