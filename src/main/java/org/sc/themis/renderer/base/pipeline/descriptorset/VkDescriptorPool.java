package org.sc.themis.renderer.base.pipeline.descriptorset;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;

import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.base.exception.FullDescriptorsetPoolException;
import org.sc.themis.renderer.lang.Vulkan;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;

public class VkDescriptorPool extends Vulkan implements LifeCycle {

  private final VkDevice device;
  private final VkDescriptorSetLayout[] layouts;
  private final int size;
  private int created = 0;
  private long handle;

  public VkDescriptorPool(VkDevice device, int size, VkDescriptorSetLayout... layouts) {
    this.device = device;
    this.size = size;
    this.layouts = layouts;
  }

  @Override
  public void setup() throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      Map<Integer, Integer> countersByType = countByType();
      int totalSize = countersByType.values().stream().mapToInt(i -> i).sum();
      VkDescriptorPoolSize.Buffer descriptorPoolSizes =
          createDescriptorPoolSizes(stack, countersByType);
      VkDescriptorPoolCreateInfo descriptorPoolCreateInfo =
          createDescriptorPoolCreateInfo(stack, descriptorPoolSizes, totalSize);
      this.handle = vkCreateDescriptorPool(stack, descriptorPoolCreateInfo);
    }
  }

  @Override
  public void cleanup() throws ThemisException {
   pipeline.destroyDescriptorPool(this.device.getHandle(), this.handle);
  }

  public VkDescriptorSet create() throws FullDescriptorsetPoolException {
    Assertions.isFalse(this::isFull, new FullDescriptorsetPoolException());
    this.created++;
    return new VkDescriptorSet(this.device, this, this.layouts);
  }

  public boolean isFull() {
    return this.created >= this.size;
  }

  public long getHandle() {
    return this.handle;
  }

  private VkDescriptorPoolCreateInfo createDescriptorPoolCreateInfo(MemoryStack stack, VkDescriptorPoolSize.Buffer descriptorPoolSizes, int totalSize) {
    return VkDescriptorPoolCreateInfo.calloc(stack)
        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
        .flags(VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT)
        .pPoolSizes(descriptorPoolSizes)
        .maxSets(totalSize);
  }

  private VkDescriptorPoolSize.Buffer createDescriptorPoolSizes(MemoryStack stack, Map<Integer, Integer> countersByType) {

    VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.calloc(countersByType.size(), stack);
    int idx = 0;

    for (Integer type : countersByType.keySet()) {
      poolSizes.get(idx++).type(type).descriptorCount(countersByType.get(type));
    }

    return poolSizes;

  }

  private Map<Integer, Integer> countByType() {

    Map<Integer, Integer> counters = new HashMap<>();

    for (VkDescriptorSetLayout layout : layouts) {
      for (int i = 0; i < layout.size(); i++) {

        VkDescriptorSetBinding binding = layout.getBinding(i);

        if (counters.containsKey(binding.getDescriptorType())) {
          counters.compute(binding.getDescriptorType(), (k, value) -> value + this.size);
        } else {
          counters.put(binding.getDescriptorType(), this.size);
        }
      }
    }

    return counters;
  }

  private long vkCreateDescriptorPool(MemoryStack stack, VkDescriptorPoolCreateInfo descriptorPoolCreateInfo) throws ThemisException {
    LongBuffer pDescriptorPool = stack.mallocLong(1);
   pipeline.createDescriptorPool(this.device.getHandle(), descriptorPoolCreateInfo, pDescriptorPool);
    return pDescriptorPool.get(0);
  }
}
