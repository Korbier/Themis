package org.sc.themis.renderer.base.queue;

import static org.lwjgl.vulkan.VK10.VK_QUEUE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK13.VK_QUEUE_GRAPHICS_BIT;

import java.util.function.Predicate;

public class VkQueueSelectors {

  public static final Predicate<VkQueueFamily> SELECTOR_GRAPHIC_QUEUE =
      vkQueueFamily -> hasFlag(vkQueueFamily, VK_QUEUE_GRAPHICS_BIT);
  public static final Predicate<VkQueueFamily> SELECTOR_COMPUTE_QUEUE =
      vkQueueFamily -> hasFlag(vkQueueFamily, VK_QUEUE_COMPUTE_BIT);
  public static final Predicate<VkQueueFamily> SELECTOR_TRANSFERT_QUEUE =
      vkQueueFamily -> hasFlag(vkQueueFamily, VK_QUEUE_TRANSFER_BIT);

  private static boolean hasFlag(VkQueueFamily properties, int flag) {
    return (properties.properties().queueFlags() & flag) != 0;
  }
}
