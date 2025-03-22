package org.sc.themis.renderer.base.device;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.lang.VulkanObject;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.slf4j.LoggerFactory;

public class VkPhysicalDevices extends VulkanObject {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VkPhysicalDevices.class);

  private final Set<Long> physicalDevices = new HashSet<>();
  private final VkInstance instance;

  public VkPhysicalDevices(Configuration configuration, VkInstance instance) {
    super(configuration);
    this.instance = instance;
  }

  @Override
  public void setup() throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      this.physicalDevices.addAll(fetchPhysicalDevices(stack));
    }
  }

  @Override
  public void cleanup() {}

  public VkPhysicalDevice select(Predicate<VkPhysicalDevice> selector) throws ThemisException {

    for (Long deviceHandle : getPhysicalDevices()) {

      org.lwjgl.vulkan.VkPhysicalDevice handle =
          new org.lwjgl.vulkan.VkPhysicalDevice(deviceHandle, this.instance.getHandle());

      VkPhysicalDevice device = new VkPhysicalDevice(getConfiguration(), handle);
      device.setup();

      if (selector.test(device)) {
        return device;
      }

      device.cleanup();

    }

    return null;

  }

  public Set<Long> getPhysicalDevices() {
    return this.physicalDevices;
  }

  public VkInstance getInstance() {
    return this.instance;
  }

  private List<Long> fetchPhysicalDevices(MemoryStack stack) throws ThemisException {

    List<Long> physicalDevices = new ArrayList<>();

    PointerBuffer pPhysicalDevices = vkFetchPhysicalDevices(stack);
    int numDevices = pPhysicalDevices.capacity();

    if (numDevices > 0) {
      for (int i = 0; i < numDevices; i++) {
        this.physicalDevices.add(pPhysicalDevices.get(i));
      }
    }

    logger.trace("{} physical devices found", this.physicalDevices.size());

    return physicalDevices;

  }

  private PointerBuffer vkFetchPhysicalDevices(MemoryStack stack) throws ThemisException {

    PointerBuffer pPhysicalDevices;

    // Get number of physical devices
    IntBuffer intBuffer = stack.mallocInt(1);
    vkPhysicalDevice().enumeratePhysicalDevices(this.instance.getHandle(), intBuffer, null);

    int numDevices = intBuffer.get(0);

    // Populate physical devices list pointer
    pPhysicalDevices = stack.mallocPointer(numDevices);
    vkPhysicalDevice()
        .enumeratePhysicalDevices(this.instance.getHandle(), intBuffer, pPhysicalDevices);

    return pPhysicalDevices;
  }
}
