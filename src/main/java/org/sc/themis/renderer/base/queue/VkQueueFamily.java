package org.sc.themis.renderer.base.queue;

import org.lwjgl.vulkan.VkQueueFamilyProperties;

public record VkQueueFamily(int handle, VkQueueFamilyProperties properties) {}
