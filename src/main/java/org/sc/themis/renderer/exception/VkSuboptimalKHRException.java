package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.KHRSwapchain;

public class VkSuboptimalKHRException extends VulkanException {

    public VkSuboptimalKHRException() {
        super(KHRSwapchain.VK_SUBOPTIMAL_KHR, "Suboptimal");
    }

}

