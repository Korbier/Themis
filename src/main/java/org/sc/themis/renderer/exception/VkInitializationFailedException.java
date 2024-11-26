package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK10;

public class VkInitializationFailedException extends VulkanException {

    public VkInitializationFailedException() {
        super(VK10.VK_ERROR_INITIALIZATION_FAILED, "Initialization failed");
    }

}

