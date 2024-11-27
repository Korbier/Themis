package org.sc.themis.renderer.queue;

import java.util.function.Predicate;

import static org.lwjgl.vulkan.VK10.VK_QUEUE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK13.VK_QUEUE_GRAPHICS_BIT;

public class VkQueueSelectors {

    public final static Predicate<VkQueueFamily> SELECTOR_GRAPHIC_QUEUE = vkQueueFamily -> hasFlag(vkQueueFamily, VK_QUEUE_GRAPHICS_BIT);
    public final static Predicate<VkQueueFamily> SELECTOR_COMPUTE_QUEUE = vkQueueFamily -> hasFlag(vkQueueFamily, VK_QUEUE_COMPUTE_BIT);
    public final static Predicate<VkQueueFamily> SELECTOR_TRANSFERT_QUEUE = vkQueueFamily -> hasFlag(vkQueueFamily, VK_QUEUE_TRANSFER_BIT);

    private static boolean hasFlag( VkQueueFamily properties, int flag ) {
        return (properties.properties().queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0;
    }

}
