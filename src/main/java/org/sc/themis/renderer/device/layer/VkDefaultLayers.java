package org.sc.themis.renderer.device.layer;

import java.util.Arrays;

public enum VkDefaultLayers implements VkLayer {

    GOOGLE_THREADING("VK_LAYER_GOOGLE_threading"),
    KHRONOS_PROFILES("VK_LAYER_KHRONOS_profiles"),
    KHRONOS_SHADER_OBJECT("VK_LAYER_KHRONOS_shader_object"),
    KHRONOS_SYNCHRONIZATION2("VK_LAYER_KHRONOS_synchronization2"),
    KHRONOS_VALIDATION("VK_LAYER_KHRONOS_validation"),
    LUNARG_API_DUMP("VK_LAYER_LUNARG_api_dump"),
    LUNARG_CORE_VALIDATION("VK_LAYER_LUNARG_core_validation"),
    LUNARG_GFXRECONSTRUCT("VK_LAYER_LUNARG_gfxreconstruct"),
    LUNARG_MONITOR("VK_LAYER_LUNARG_monitor"),
    LUNARG_OBJECT_VALIDATION("VK_LAYER_LUNARG_object_tracker"),
    LUNARG_PARAMETER_VALIDATION("VK_LAYER_LUNARG_parameter_validation"),
    LUNARG_SCREENSHOT("VK_LAYER_LUNARG_screenshot"),
    LUNARG_STANDARD_VALIDATION("VK_LAYER_LUNARG_standard_validation"),
    LUNARG_UNIQUE_VALIDATION("VK_LAYER_LUNARG_unique_validation"),
    UNKNOWN("UNKNOWN");

    private final String name;

    VkDefaultLayers(String name ) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static VkDefaultLayers fromName(String name ) {
        return Arrays.stream(values()).filter( layer -> layer.getName().equals(name) ).findAny().orElse( VkDefaultLayers.UNKNOWN );
    }

}
