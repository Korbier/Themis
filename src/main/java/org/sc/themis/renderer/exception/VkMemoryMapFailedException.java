package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK10;

public class VkMemoryMapFailedException extends VulkanException {

    public VkMemoryMapFailedException() {
        super(VK10.VK_ERROR_MEMORY_MAP_FAILED, "Memory map failed");
    }

}

