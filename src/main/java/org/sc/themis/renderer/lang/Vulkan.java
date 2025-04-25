package org.sc.themis.renderer.lang;

public class Vulkan {

  protected final VulkanDebug debug = new VulkanDebug();
  protected final VulkanInstance instance = new VulkanInstance();
  protected final VulkanPhysicalDevice physicalDevice = new VulkanPhysicalDevice();
  protected final VulkanDevice device = new VulkanDevice();
  protected final VulkanMemoryAllocator memoryAllocator = new VulkanMemoryAllocator();
  protected final VulkanSurface surface = new VulkanSurface();
  protected final VulkanImage image = new VulkanImage();
  protected final VulkanSync sync = new VulkanSync();
  protected final VulkanFramebuffer framebuffer = new VulkanFramebuffer();
  protected final VulkanRenderPass renderPass = new VulkanRenderPass();
  protected final VulkanPipeline pipeline = new VulkanPipeline();
  protected final VulkanCommand command = new VulkanCommand();

}
