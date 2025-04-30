package org.sc.themis.renderer.base.presentation;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import org.sc.themis.core.LifeCycle;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.base.exception.SurfaceFormatNotFoundException;
import org.sc.themis.renderer.base.queue.VkQueue;
import org.sc.themis.renderer.base.resource.image.VkImageView;
import org.sc.themis.renderer.base.resource.image.VkImageViewDescriptor;
import org.sc.themis.renderer.base.sync.VkSemaphore;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.renderer.lang.exception.VkOutOfDateKHRException;
import org.sc.themis.renderer.lang.exception.VkSuboptimalKHRException;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.configuration.ConfigurationEnum;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.LogUtils;
import org.sc.themis.window.Window;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public class VkSwapChain extends Vulkan implements LifeCycle {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VkSwapChain.class);

  private final Window window;
  private final VkDevice device;
  private final VkSurface vkSurface;
  private final VkQueue presentQueue;
  private final VkQueue[] concurrentQueues;
  private final int frameCount;
  private final boolean enableVSync;

  private long handle;

  private VkImageView[] imageViews;
  private VkSurfaceFormat surfaceFormat;
  private VkExtent2D swapChainExtent;

  private int currentFrame = 0;

  public VkSwapChain(Window window, VkDevice device, VkSurface surface, int frameCount, boolean enableVSync, VkQueue presentQueue, VkQueue... concurrentQueues) {
    this.window = window;
    this.device = device;
    this.vkSurface = surface;
    this.frameCount = frameCount;
    this.enableVSync = enableVSync;
    this.presentQueue = presentQueue;
    this.concurrentQueues = concurrentQueues;
  }

  @Override
  public void setup() throws ThemisException {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkSurfaceCapabilitiesKHR surfaceCapabilities = vkRetrieveSurfaceCapabilities(stack);
      int imageCount = retrieveImageCount(surfaceCapabilities);

      setupSurfaceFormat();
      setupSwapChainExtent(surfaceCapabilities);
      setupSwapChain(surfaceCapabilities, imageCount);
      setupImageViews();

      this.currentFrame = 0;

      logger.trace("SwapChain initialized ({}, views : {}).", this, this.imageViews.length);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{handle=" + LogUtils.toHexString(this.handle) + "}";
  }

  private void setupImageViews() throws ThemisException {
    this.imageViews = createImageViews(this.surfaceFormat.imageFormat());
  }

  private void setupSwapChain(VkSurfaceCapabilitiesKHR surfaceCapabilities, int imageCount) throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkSwapchainCreateInfoKHR swapChainCreateInfo = createSwapChainCreateInfo(stack, surfaceCapabilities, imageCount);
      this.handle = vkCreateSwapChain(stack, swapChainCreateInfo);
    }
  }

  private void setupSwapChainExtent(VkSurfaceCapabilitiesKHR surfaceCapabilities) {

    VkExtent2D extent = VkExtent2D.calloc();

    if (surfaceCapabilities.currentExtent().width() == 0xFFFFFFFF) {

      // Surface size undefined. Set to the window size if within bounds
      int width = Math.min(this.window.getSize().x, surfaceCapabilities.maxImageExtent().width());
      width = Math.max(width, surfaceCapabilities.minImageExtent().width());

      int height = Math.min(this.window.getSize().y, surfaceCapabilities.maxImageExtent().height());
      height = Math.max(height, surfaceCapabilities.minImageExtent().height());

      extent.width(width);
      extent.height(height);

    } else {
      // Surface already defined, just use that for the swap chain
      extent.set(surfaceCapabilities.currentExtent());
    }

    this.swapChainExtent = extent;
  }

  private void setupSurfaceFormat() throws ThemisException {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      VkSurfaceFormatKHR.Buffer surfaceFormats = vkRetrieveSurfaceFormats(stack);

      for (int i = 0; i < surfaceFormats.remaining(); i++) {
        VkSurfaceFormatKHR surfaceFormatKHR = surfaceFormats.get(i);
        if (surfaceFormatKHR.format() == VK_FORMAT_B8G8R8A8_SRGB && surfaceFormatKHR.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
          this.surfaceFormat = new VkSurfaceFormat(surfaceFormatKHR.format(), surfaceFormatKHR.colorSpace());
          return;
        }
      }

      this.surfaceFormat = new VkSurfaceFormat(VK_FORMAT_B8G8R8A8_SRGB, surfaceFormats.get(0).colorSpace());
    }
  }

  @Override
  public void cleanup() throws ThemisException {

    this.swapChainExtent.free();

    for (VkImageView imageView : this.imageViews) {
      imageView.cleanup();
    }

   surface.destroySwapchainKHR(this.device.getHandle(), this.handle);
  }

  public VkSurfaceFormat getSurfaceFormat() {
    return this.surfaceFormat;
  }

  public int getFrameCount() {
    return this.imageViews.length;
  }

  public int getCurrentFrame() {
    return this.currentFrame;
  }

  public VkImageView getImageView(int frame) {
    return this.imageViews[frame];
  }

  public VkExtent2D getExtent() {
    return this.swapChainExtent;
  }

  public boolean acquire(MemoryStack stack, VkSemaphore acquireSemaphore) throws ThemisException {

    boolean resize = false;

    try {
      IntBuffer ip = stack.mallocInt(1);
     surface.acquireNextImageKHR(this.device.getHandle(), this.handle, ~0L, acquireSemaphore.getHandle(), MemoryUtil.NULL, ip);
      this.currentFrame = ip.get(0);
    } catch (VkOutOfDateKHRException e) {
      resize = true;
    } catch (VkSuboptimalKHRException e) {
      // Not optimal but swapchain can still be used
    }

    return resize;
  }

  public boolean present(MemoryStack stack, VkSemaphore presentSemaphore) throws ThemisException {

    boolean resize = false;

    try {
      VkPresentInfoKHR presentInfo = createPresentInfo(stack, presentSemaphore);
     surface.queuePresentKHR(this.presentQueue.getHandle(), presentInfo);
    } catch (VkOutOfDateKHRException e) {
      resize = true;
    } catch (VkSuboptimalKHRException e) {
      // Not optimal but swapchain can still be used
    }

    this.currentFrame = (this.currentFrame + 1) % this.imageViews.length;

    return resize;
  }

  private VkPresentInfoKHR createPresentInfo(MemoryStack stack, VkSemaphore presentSemaphore) {
    return VkPresentInfoKHR
        .calloc(stack)
        .sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
        .pWaitSemaphores(presentSemaphore != null ? stack.longs(presentSemaphore.getHandle()) : null)
        .swapchainCount(1)
        .pSwapchains(stack.longs(this.handle))
        .pImageIndices(stack.ints(this.currentFrame));
  }

  private VkImageView[] createImageViews(int format) throws ThemisException {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      LongBuffer swapChainImages = retrieveSwapChainImages(stack);
      VkImageView[] imageViews = new VkImageView[swapChainImages.remaining()];
      VkImageViewDescriptor descriptor = new VkImageViewDescriptor(VK_IMAGE_ASPECT_COLOR_BIT, 0, format, 1, 1, VK_IMAGE_VIEW_TYPE_2D);

      for (int i = 0; i < swapChainImages.remaining(); i++) {
        imageViews[i] = new VkImageView(device, swapChainImages.get(i), descriptor);
        imageViews[i].setup();
      }

      return imageViews;
    }
  }

  private LongBuffer retrieveSwapChainImages(MemoryStack stack) throws ThemisException {

    IntBuffer ip = stack.mallocInt(1);
   surface.getSwapchainImagesKHR(this.device.getHandle(), this.handle, ip, null);

    LongBuffer swapChainImages = stack.mallocLong(ip.get(0));
   surface.getSwapchainImagesKHR(this.device.getHandle(), this.handle, ip, swapChainImages);

    return swapChainImages;
  }

  private long vkCreateSwapChain(MemoryStack stack, VkSwapchainCreateInfoKHR swapChainCreateInfo) throws ThemisException {
    LongBuffer lp = stack.mallocLong(1);
   surface.createSwapchainKHR(this.device.getHandle(), swapChainCreateInfo, lp);
    return lp.get(0);
  }

  private VkSwapchainCreateInfoKHR createSwapChainCreateInfo(MemoryStack stack, VkSurfaceCapabilitiesKHR surfaceCapabilities, int imageCount) throws ThemisException {

    VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack).sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR).surface(vkSurface.getHandle()).minImageCount(imageCount).imageFormat(surfaceFormat.imageFormat()).imageColorSpace(surfaceFormat.colorSpace()).imageExtent(swapChainExtent).imageArrayLayers(1).imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT).imageSharingMode(VK_SHARING_MODE_EXCLUSIVE).preTransform(surfaceCapabilities.currentTransform()).compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR).clipped(true);

    if (this.enableVSync) {
      swapchainCreateInfo.presentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR);
    } else {
      swapchainCreateInfo.presentMode(KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR);
    }

    List<VkQueue> concurrentQueues = retrieveSharedQueue();

    if (concurrentQueues.isEmpty()) {
      swapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
    } else {

      IntBuffer intBuffer = stack.mallocInt(concurrentQueues.size() + 1);
      concurrentQueues.forEach(i -> intBuffer.put(i.getQueueFamilyIndex()));
      intBuffer.put(this.presentQueue.getQueueFamilyIndex());
      intBuffer.flip();

      swapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT).queueFamilyIndexCount(intBuffer.capacity()).pQueueFamilyIndices(intBuffer);
    }

    return swapchainCreateInfo;
  }

  private List<VkQueue> retrieveSharedQueue() {
    return Arrays.stream(this.concurrentQueues).filter(q -> q.getQueueFamilyIndex() != this.presentQueue.getQueueFamilyIndex()).toList();
  }

  private VkSurfaceFormatKHR.Buffer vkRetrieveSurfaceFormats(MemoryStack stack) throws ThemisException {

    IntBuffer ip = stack.mallocInt(1);
   surface.getPhysicalDeviceSurfaceFormatsKHR(this.device.getPhysicalDevice().getHandle(), this.vkSurface.getHandle(), ip, null);

    int numFormats = ip.get(0);
    if (numFormats <= 0) {
      throw new SurfaceFormatNotFoundException();
    }

    VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(numFormats, stack);
   surface.getPhysicalDeviceSurfaceFormatsKHR(this.device.getHandle().getPhysicalDevice(), this.vkSurface.getHandle(), ip, surfaceFormats);

    return surfaceFormats;
  }

  private int retrieveImageCount(VkSurfaceCapabilitiesKHR capabilities) {

    int maxImages = capabilities.maxImageCount();
    int minImages = capabilities.minImageCount();

    int result = minImages;

    if (maxImages != 0) {
      result = Math.min(this.frameCount, maxImages);
    }

    result = Math.max(result, minImages);

    return result;

  }

  private VkSurfaceCapabilitiesKHR vkRetrieveSurfaceCapabilities(MemoryStack stack) throws ThemisException {
    VkSurfaceCapabilitiesKHR surfCapabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
    surface.getPhysicalDeviceSurfaceCapabilities(this.device.getPhysicalDevice().getHandle(), this.vkSurface.getHandle(), surfCapabilities);
    return surfCapabilities;
  }
}
