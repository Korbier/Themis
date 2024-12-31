package org.sc.themis.scene;

public interface MeshProperty<D> {

    static <T> MeshProperty<T> of(Class<T> clazz ) {
        return () -> clazz;
    }

    Class<D> getType();

}
