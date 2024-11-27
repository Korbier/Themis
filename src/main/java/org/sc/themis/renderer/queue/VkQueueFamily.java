package org.sc.themis.renderer.queue;

import org.lwjgl.vulkan.VkQueueFamilyProperties;

public record VkQueueFamily(int handle, VkQueueFamilyProperties properties) {

}
