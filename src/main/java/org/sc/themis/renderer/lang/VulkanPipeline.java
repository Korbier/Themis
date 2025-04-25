package org.sc.themis.renderer.lang;

import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCopyDescriptorSet;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import org.sc.themis.renderer.lang.exception.VkFragmentedPoolException;
import org.sc.themis.renderer.lang.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.lang.exception.VkOutOfHostMemoryException;
import org.sc.themis.renderer.lang.exception.VkOutOfPoolMemoryException;
import org.sc.themis.shared.exception.ThemisException;

public class VulkanPipeline extends VulkanBackend {

  public void createShaderModule(
      VkDevice device, VkShaderModuleCreateInfo pCreateInfo, LongBuffer pShaderModule)
      throws ThemisException {
    vk(
        () -> vkCreateShaderModule(device, pCreateInfo, null, pShaderModule),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroyShaderModule(VkDevice device, long shaderModule) throws ThemisException {
    vk(() -> vkDestroyShaderModule(device, shaderModule, null));
  }

  public void createGraphicsPipelines(
      VkDevice device,
      long pipelineCache,
      VkGraphicsPipelineCreateInfo.Buffer pCreateInfos,
      LongBuffer pPipelines)
      throws ThemisException {
    vk(
        () -> vkCreateGraphicsPipelines(device, pipelineCache, pCreateInfos, null, pPipelines),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroyPipeline(VkDevice device, long pipeline) throws ThemisException {
    vk(() -> vkDestroyPipeline(device, pipeline, null));
  }

  public void createPipelineLayout(
      VkDevice device, VkPipelineLayoutCreateInfo pCreateInfo, LongBuffer pPipelineLayout)
      throws ThemisException {
    vk(
        () -> vkCreatePipelineLayout(device, pCreateInfo, null, pPipelineLayout),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroyPipelineLayout(VkDevice device, long pipelineLayout) throws ThemisException {
    vk(() -> vkDestroyPipelineLayout(device, pipelineLayout, null));
  }

  public void createDescriptorSetLayout(
      VkDevice device, VkDescriptorSetLayoutCreateInfo pCreateInfo, LongBuffer pSetLayout)
      throws ThemisException {
    vk(
        () -> vkCreateDescriptorSetLayout(device, pCreateInfo, null, pSetLayout),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroyDescriptorSetLayout(VkDevice device, long descriptorSetLayout)
      throws ThemisException {
    vk(() -> vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null));
  }

  public void createDescriptorPool(
      VkDevice device, VkDescriptorPoolCreateInfo pCreateInfo, LongBuffer pDescriptorPool)
      throws ThemisException {
    vk(
        () -> vkCreateDescriptorPool(device, pCreateInfo, null, pDescriptorPool),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
        });
  }

  public void destroyDescriptorPool(VkDevice device, long descriptorPool) throws ThemisException {
    vk(() -> vkDestroyDescriptorPool(device, descriptorPool, null));
  }

  public void allocateDescriptorSets(
      VkDevice device, VkDescriptorSetAllocateInfo pAllocateInfo, LongBuffer pDescriptorSets)
      throws ThemisException {
    vk(
        () -> vkAllocateDescriptorSets(device, pAllocateInfo, pDescriptorSets),
        (errno) -> {
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY)
            throw new VkOutOfHostMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY)
            throw new VkOutOfDeviceMemoryException();
          if (errno == org.lwjgl.vulkan.VK10.VK_ERROR_FRAGMENTED_POOL)
            throw new VkFragmentedPoolException();
          if (errno == org.lwjgl.vulkan.VK11.VK_ERROR_OUT_OF_POOL_MEMORY)
            throw new VkOutOfPoolMemoryException();
        });
  }

  public void cmdBindDescriptorSets(
      VkCommandBuffer commandBuffer,
      int pipelineBindPoint,
      long layout,
      int firstSet,
      LongBuffer pDescriptorSets,
      IntBuffer pDynamicOffsets)
      throws ThemisException {
    vk(
        () ->
            vkCmdBindDescriptorSets(
                commandBuffer,
                pipelineBindPoint,
                layout,
                firstSet,
                pDescriptorSets,
                pDynamicOffsets));
  }

  public void updateDescriptorSets(
      VkDevice device,
      VkWriteDescriptorSet.Buffer pDescriptorWrites,
      VkCopyDescriptorSet.Buffer pDescriptorCopies)
      throws ThemisException {
    vk(() -> vkUpdateDescriptorSets(device, pDescriptorWrites, pDescriptorCopies));
  }
}
