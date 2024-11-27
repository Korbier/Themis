package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK10;

public class VkExtensionNotPresentException extends VulkanException {

    public VkExtensionNotPresentException() {
        super(VK10.VK_ERROR_EXTENSION_NOT_PRESENT, "Extension not present");
    }

}

