package org.sc.themis.renderer.base.device;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_TRUE;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.sc.themis.renderer.base.exception.NoQueueFamilyFoundException;
import org.sc.themis.renderer.base.presentation.VkSurface;
import org.sc.themis.renderer.base.queue.VkQueue;
import org.sc.themis.renderer.base.queue.VkQueueFamily;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.configuration.ConfigurationEnum;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;
import org.sc.themis.shared.utils.BitwiseState;
import org.sc.themis.shared.utils.LogUtils;
import org.slf4j.LoggerFactory;

public class VkDevice extends Vulkan implements LifeCycle {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VkDevice.class);

  public static final int FEATURE_SAMPLER_ANISOTROPY          = 0b0000_0000_0000_0000_0000_0000_0000_0001;
  public static final int FEATURE_GEOMETRY_SHADER             = 0b0000_0000_0000_0000_0000_0000_0000_0010;
  public static final int FEATURE_FRAGMENT_STORES_AND_ATOMICS = 0b0000_0000_0000_0000_0000_0000_0000_0011;

  private final boolean enableFeatureSamplerAnisotropy;
  private final boolean enableFeatureGeometryShader;
  private final boolean enableFeatureFragmentStoresAndAtomics;

  private final VkPhysicalDevice physicalDevice;
  private org.lwjgl.vulkan.VkDevice handle;

  private final BitwiseState features = new BitwiseState();

  public VkDevice(VkPhysicalDevice physicalDevice, boolean enableFeatureSamplerAnisotropy, boolean enableFeatureGeometryShader, boolean enableFeatureFragmentStoresAndAtomics) {
    this.physicalDevice = physicalDevice;
    this.enableFeatureSamplerAnisotropy = enableFeatureSamplerAnisotropy;
    this.enableFeatureGeometryShader = enableFeatureGeometryShader;
    this.enableFeatureFragmentStoresAndAtomics = enableFeatureFragmentStoresAndAtomics;
  }

  public org.lwjgl.vulkan.VkDevice getHandle() {
    return this.handle;
  }

  public VkPhysicalDevice getPhysicalDevice() {
    return this.physicalDevice;
  }

  @Override
  public void setup() throws ThemisException {
    setupVkDevice();
    logger.trace("Device initialized (handle={})", LogUtils.toHexString(this.handle.address()));
  }

  @Override
  public void cleanup() throws ThemisException {
    cleanupVkDevice();
  }

  public void waitIdle() throws ThemisException {
    device.deviceWaitIdle(this.getHandle());
  }

  public VkQueue selectQueue(int queueIndex, Predicate<VkQueueFamily> selector)
      throws ThemisException {

    int queueFamilyIndex = selectQueueFamily(selector);
    org.lwjgl.vulkan.VkQueue vkQueue = vkFetchQueue(queueIndex, queueFamilyIndex);

    VkQueue queue = new VkQueue(vkQueue, queueFamilyIndex);
    queue.setup();

    return queue;
  }

  public VkQueue selectPresentQueue(int queueIndex, VkSurface surface) throws ThemisException {

    Predicate<VkQueueFamily> selector =
        (_) -> {
          try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer intBuff = stack.mallocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(this.physicalDevice.getHandle(), queueIndex, surface.getHandle(), intBuff);
            return intBuff.get(0) == VK_TRUE;
          }
        };

    return selectQueue(queueIndex, selector);

  }

  public boolean isFeatureEnabled(int feature) {
    return this.features.isset(feature);
  }

  private void setupVkDevice() throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      PointerBuffer requiredExtensions = selectVkExtensions(stack);
      VkPhysicalDeviceFeatures requiredFeatures = selectVkFeatures(stack);
      VkDeviceQueueCreateInfo.Buffer queueCreateInfo = createQueueCreateInfo(stack);
      VkDeviceCreateInfo deviceCreateInfo = createDeviceCreateInfo(stack, requiredExtensions, requiredFeatures, queueCreateInfo);
      this.handle = this.vkCreateDevice(stack, deviceCreateInfo);
    }
  }

  private void cleanupVkDevice() throws ThemisException {
    device.destroyDevice(this.getHandle());
  }

  private PointerBuffer selectVkExtensions(MemoryStack stack) {

    List<String> extensions = new ArrayList<>();
    extensions.add(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);

    PointerBuffer required = stack.mallocPointer(extensions.size());
    for (String extension : extensions) {
      required.put(stack.ASCII(extension));
    }

    required.flip();

    return required;
  }

  private VkPhysicalDeviceFeatures selectVkFeatures(MemoryStack stack) {

    VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc(stack);

    logger.debug("Enabled features : ");

    if (this.enableFeatureSamplerAnisotropy && this.physicalDevice.getFeatures().samplerAnisotropy()) {
      logger.debug(". Sampler anisotropy");
      this.features.set(FEATURE_SAMPLER_ANISOTROPY);
      features.samplerAnisotropy(true);
    }

    if (this.enableFeatureGeometryShader && this.physicalDevice.getFeatures().geometryShader()) {
      logger.debug(". Geometry shader feature enabled");
      this.features.set(FEATURE_GEOMETRY_SHADER);
      features.geometryShader(true);
    }

    if (this.enableFeatureFragmentStoresAndAtomics && this.physicalDevice.getFeatures().fragmentStoresAndAtomics()) {
      logger.debug(". Fragment stores and atomics");
      this.features.set(FEATURE_FRAGMENT_STORES_AND_ATOMICS);
      features.fragmentStoresAndAtomics(true);
    }

    return features;
  }

  private VkDeviceQueueCreateInfo.Buffer createQueueCreateInfo(MemoryStack stack) {

    VkQueueFamilyProperties.Buffer queueFamilyProperties = this.physicalDevice.getQueueFamilyProperties();
    int numQueuesFamilies = queueFamilyProperties.capacity();
    VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(numQueuesFamilies, stack);

    for (int i = 0; i < numQueuesFamilies; i++) {
      FloatBuffer priorities = stack.callocFloat(queueFamilyProperties.get(i).queueCount());
      queueCreateInfos
          .get(i)
          .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
          .queueFamilyIndex(i)
          .pQueuePriorities(priorities);
    }

    return queueCreateInfos;

  }

  private VkDeviceCreateInfo createDeviceCreateInfo(
      MemoryStack stack, PointerBuffer requiredExtensions,
      VkPhysicalDeviceFeatures requiredFeatures, VkDeviceQueueCreateInfo.Buffer queueCreateInfo
  ) {
    return VkDeviceCreateInfo.calloc(stack)
        .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
        .ppEnabledExtensionNames(requiredExtensions)
        .pEnabledFeatures(requiredFeatures)
        .pQueueCreateInfos(queueCreateInfo);
  }

  private org.lwjgl.vulkan.VkDevice vkCreateDevice(MemoryStack stack, VkDeviceCreateInfo deviceCreateInfo) throws ThemisException {
    PointerBuffer pp = stack.mallocPointer(1);
    device.createDevice(this.physicalDevice.getHandle(), deviceCreateInfo, pp);
    return new org.lwjgl.vulkan.VkDevice(pp.get(0), this.physicalDevice.getHandle(), deviceCreateInfo);
  }

  private int selectQueueFamily(Predicate<VkQueueFamily> selector) throws NoQueueFamilyFoundException {
    return getPhysicalDevice()
        .selectQueueFamily(selector)
        .orElseThrow(NoQueueFamilyFoundException::new)
        .handle();
  }

  private org.lwjgl.vulkan.VkQueue vkFetchQueue(int queueIndex, int queueFamilyIndex) throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      PointerBuffer pQueue = stack.mallocPointer(1);
      device.getDeviceQueue(getHandle(), queueFamilyIndex, queueIndex, pQueue);
      return new org.lwjgl.vulkan.VkQueue(pQueue.get(0), getHandle());
    }
  }
}
