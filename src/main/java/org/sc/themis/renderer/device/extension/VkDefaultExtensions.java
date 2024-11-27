package org.sc.themis.renderer.device.extension;

import org.lwjgl.vulkan.EXTDebugUtils;

import java.util.Arrays;

public enum VkDefaultExtensions implements VkExtension {

    KHR_PORTABILITY_ENUMERATION("VK_KHR_portability_enumeration"),
    EXT_DEBUG_UTILS_EXTENSION_NAME(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME),
    UNKNOWN("UNKNOWN");

    private final String name;

    VkDefaultExtensions(String name ) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static VkDefaultExtensions fromName( String name ) {
        return Arrays.stream(values()).filter( extension -> extension.getName().equals(name) ).findAny().orElse( VkDefaultExtensions.UNKNOWN );
    }

}
