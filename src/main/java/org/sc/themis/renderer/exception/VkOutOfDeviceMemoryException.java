package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK10;

public class VkOutOfDeviceMemoryException extends VulkanException {

    public VkOutOfDeviceMemoryException() {
        super(VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY, "Out of device memory");
    }

}

