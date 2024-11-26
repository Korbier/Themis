package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK10;

public class VkDeviceLostException extends VulkanException {

    public VkDeviceLostException() {
        super(VK10.VK_ERROR_DEVICE_LOST, "Device lost");
    }

}

