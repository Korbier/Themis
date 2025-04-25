package org.sc.themis.renderer.lang;

public class Vulkan {

  public final static VulkanDebug debug = new VulkanDebug();
  public final static VulkanInstance instance = new VulkanInstance();
  public final static VulkanPhysicalDevice physicalDevice = new VulkanPhysicalDevice();
  public final static VulkanDevice device = new VulkanDevice();
  public final static VulkanMemoryAllocator memoryAllocator = new VulkanMemoryAllocator();
  public final static VulkanSurface surface = new VulkanSurface();
  public final static VulkanImage image = new VulkanImage();
  public final static VulkanSync sync = new VulkanSync();
  public final static VulkanFramebuffer framebuffer = new VulkanFramebuffer();
  public final static VulkanRenderPass renderPass = new VulkanRenderPass();
  public final static VulkanPipeline pipeline = new VulkanPipeline();
  public final static VulkanCommand command = new VulkanCommand();

}
