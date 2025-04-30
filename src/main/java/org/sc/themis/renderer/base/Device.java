package org.sc.themis.renderer.base;

import org.sc.themis.core.LifeCycle;
import org.sc.themis.renderer.base.device.*;
import org.sc.themis.renderer.base.presentation.VkSurface;
import org.sc.themis.renderer.base.queue.VkQueue;
import org.sc.themis.renderer.base.queue.VkQueueFamily;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.configuration.ConfigurationEnum;
import org.sc.themis.shared.exception.ThemisException;

import java.util.function.Predicate;

public class Device implements LifeCycle {

  private final Configuration configuration;
  private final VkInstance instance;
  private VkPhysicalDevice physicalDevice;
  private VkDevice device;
  private VkMemoryAllocator memoryAllocator;

  public Device(Configuration configuration) {
    this.configuration = configuration;
    this.instance = new VkInstance(configuration);
  }

  @Override
  public void setup() throws ThemisException {
    this.instance.setup();
    this.setupPhysicalDevice();
    this.setupDevice();
    this.setupMemoryAllocator();
  }

  @Override
  public void cleanup() throws ThemisException {
    this.memoryAllocator.cleanup();
    this.device.cleanup();
    this.physicalDevice.cleanup();
    this.instance.cleanup();
  }

  public VkInstance instance() {
    return instance;
  }

  public VkPhysicalDevice physicalDevice() {
    return physicalDevice;
  }

  public VkDevice device() {
    return device;
  }

  public VkMemoryAllocator memoryAllocator() {
    return memoryAllocator;
  }

  public void waitIdle() throws ThemisException {
    this.device.waitIdle();
  }

  public VkQueue selectQueue(int defaultQueueIndex, Predicate<VkQueueFamily> selectorGraphicQueue) throws ThemisException {
    return this.device.selectQueue(defaultQueueIndex, selectorGraphicQueue);
  }

  public VkQueue selectPresentQueue(int defaultQueueIndex, VkSurface surface) throws ThemisException {
    return this.device.selectPresentQueue(defaultQueueIndex, surface);
  }

  private void setupMemoryAllocator() throws ThemisException {
    this.memoryAllocator = new VkMemoryAllocator(this.physicalDevice, this.device, this.instance);
    this.memoryAllocator.setup();
  }

  private void setupDevice() throws ThemisException {

    this.device = new VkDevice(
        this.physicalDevice,
        this.configuration.get(ConfigurationEnum.rendererFeatureSamplerAnisotropy, false),
        this.configuration.get(ConfigurationEnum.rendererFeatureGeometryShader, false),
        this.configuration.get(ConfigurationEnum.rendererFeatureFragmentStoresAndAtomics, false)
    );

    this.device.setup();

  }

  private void setupPhysicalDevice() throws ThemisException {
    VkPhysicalDevices devices = new VkPhysicalDevices(this.instance);
    try {
      devices.setup();
      this.physicalDevice = devices.select(VkPhysicalDeviceSelectors.hasGraphicsQueue.and(VkPhysicalDeviceSelectors.hasKHRSwapChainExtension));
    } finally {
      devices.cleanup();
    }
  }

}
