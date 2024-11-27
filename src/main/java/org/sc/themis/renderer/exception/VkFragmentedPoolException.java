package org.sc.themis.renderer.exception;

import org.lwjgl.vulkan.VK10;

public class VkFragmentedPoolException extends VulkanException {

    public VkFragmentedPoolException() {
        super(VK10.VK_ERROR_FRAGMENTED_POOL, "Fragmented Pool");
    }

}

