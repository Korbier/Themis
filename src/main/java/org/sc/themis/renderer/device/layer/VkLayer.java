package org.sc.themis.renderer.device.layer;

public interface VkLayer {

    static VkLayer of( String name ) {
        return () -> name;
    }

    String getName();

}
