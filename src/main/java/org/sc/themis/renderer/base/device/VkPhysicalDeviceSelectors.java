package org.sc.themis.renderer.base.device;

import java.util.function.Predicate;
import org.lwjgl.vulkan.KHRSwapchain;
import org.sc.themis.renderer.base.queue.VkQueueSelectors;

public class VkPhysicalDeviceSelectors {

  public static final Predicate<VkPhysicalDevice> hasKHRSwapChainExtension = device -> device.hasExtension(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);
  public static final Predicate<VkPhysicalDevice> hasGraphicsQueue = device -> device.selectQueueFamily(VkQueueSelectors.SELECTOR_GRAPHIC_QUEUE).isPresent();

}
