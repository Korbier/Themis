package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK10;

public class VkOutOfHostMemoryException extends VulkanException {

    public VkOutOfHostMemoryException() {
        super(VK10.VK_ERROR_OUT_OF_HOST_MEMORY, "Out of host memory");
    }

}

