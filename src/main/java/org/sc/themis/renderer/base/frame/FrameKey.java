package org.sc.themis.renderer.base.frame;

import org.sc.themis.renderer.base.VulkanObject;

public interface FrameKey<D extends VulkanObject> {

    static <T extends VulkanObject> FrameKey<T> of( Class<T> clazz ) {
        return () -> clazz;
    }

    Class<D> getType();

}
