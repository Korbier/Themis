package org.sc.themis.scene;

import org.sc.themis.shared.exception.ThemisException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Model {

    private final String identifier;
    private final Mesh [] meshes;
    private final Material [] materials;
    private final List<Instance> instances = new ArrayList<>();

    Model( String identifier, Mesh [] meshes, Material [] materials ) {
        this.identifier = identifier;
        this.meshes = meshes;
        this.materials = materials;
    }

    public void cleanup() throws ThemisException {
        for ( Mesh mesh : getMeshes() ) {
            mesh.cleanup();
        }
        for ( Material material : getMaterials() ) {
            material.cleanup();
        }
    }

    public boolean isRenderable() {

        for ( Mesh mesh : this.getMeshes() ) {
            if ( !mesh.isRenderable() ) return false;
        }

        return true;

    }

    public Instance create() {
        Instance instance = new Instance(this);
        this.instances.add( instance );
        return instance;
    }

    public Mesh[] getMeshes() {
        return this.meshes;
    }

    public Material[] getMaterials() {
        return this.materials;
    }

    public List<Instance> getInstances() {
        return this.instances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return Objects.equals(identifier, model.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }

}
