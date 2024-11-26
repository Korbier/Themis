package org.sc.themis.renderer.device.extension;

public interface VkExtension {

    static VkExtension of(String name ) {
        return () -> name;
    }

    String getName();

}
