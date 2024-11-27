package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK11;

public class VkInvalidExternalHandleException extends VulkanException {

    public VkInvalidExternalHandleException() {
        super(VK11.VK_ERROR_INVALID_EXTERNAL_HANDLE, "Invalid external handle");
    }

}

