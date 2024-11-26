package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.KHRSwapchain;

public class VkOutOfDateKHRException extends VulkanException {

    public VkOutOfDateKHRException() {
        super(KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR, "Out of date");
    }

}

