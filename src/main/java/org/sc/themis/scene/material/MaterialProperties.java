package org.sc.themis.scene.material;

import java.util.HashMap;

public class MaterialProperties extends HashMap<MaterialProperty<?>, Object> {

    private final HashMap<String, String> variantIdentifier = new HashMap<>();

    public <T> T getProperty( MaterialProperty<T> property ) {
        return (T) get( property );
    }

    public void setVariantIdentifier(Material material, String identifier) {
        this.variantIdentifier.put( material.getIdentifier(), identifier );
    }

    public String getVariantIdentifier( Material material ) {
        return this.variantIdentifier.get( material.getIdentifier() );
    }

}
