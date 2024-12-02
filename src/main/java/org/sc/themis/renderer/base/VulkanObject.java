package org.sc.themis.renderer.base;

import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.tobject.TObject;

public abstract class VulkanObject extends TObject {

    private final VulkanDebug debug = new VulkanDebug();
    private final VulkanInstance instance = new VulkanInstance();
    private final VulkanPhysicalDevice physicalDevice = new VulkanPhysicalDevice();
    private final VulkanDevice device = new VulkanDevice();
    private final VulkanMemoryAllocator memoryAllocator = new VulkanMemoryAllocator();
    private final VulkanSurface surface = new VulkanSurface();
    private final VulkanImage image = new VulkanImage();
    private final VulkanSync sync = new VulkanSync();
    private final VulkanFramebuffer framebuffer = new VulkanFramebuffer();
    private final VulkanRenderPass renderPass = new VulkanRenderPass();

    public VulkanObject(Configuration configuration) {
        super( configuration );
    }

    protected VulkanDebug vkDebug() {
        return this.debug;
    }

    protected VulkanInstance vkInstance() {
        return this.instance;
    }

    protected VulkanPhysicalDevice vkPhysicalDevice() {
        return this.physicalDevice;
    }

    protected VulkanDevice vkDevice() {
        return this.device;
    }

    protected VulkanMemoryAllocator vkMemoryAllocator() {
        return this.memoryAllocator;
    }

    protected VulkanSurface vkSurface() {
        return this.surface;
    }

    public VulkanImage vkImage() {
        return this.image;
    }

    public VulkanSync vkSync() {
        return this.sync;
    }

    public VulkanFramebuffer vkFramebuffer() {
        return this.framebuffer;
    }

    public VulkanRenderPass vkRenderPass() {
        return this.renderPass;
    }

}
