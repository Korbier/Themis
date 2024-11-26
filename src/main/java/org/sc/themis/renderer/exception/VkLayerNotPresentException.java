package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK10;

public class VkLayerNotPresentException extends VulkanException {

    public VkLayerNotPresentException() {
        super(VK10.VK_ERROR_LAYER_NOT_PRESENT, "Layer not present");
    }

}

