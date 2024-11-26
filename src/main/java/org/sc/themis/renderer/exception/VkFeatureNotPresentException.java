package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK10;

public class VkFeatureNotPresentException extends VulkanException {

    public VkFeatureNotPresentException() {
        super(VK10.VK_ERROR_FEATURE_NOT_PRESENT, "Feature not present");
    }

}

