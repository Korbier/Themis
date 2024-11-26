package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK10;

public class VkTooManyObjectsException extends VulkanException {

    public VkTooManyObjectsException() {
        super(VK10.VK_ERROR_TOO_MANY_OBJECTS, "Too many objects");
    }

}

