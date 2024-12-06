package org.sc.themis.renderer.base;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.exception.*;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanPipeline extends Vulkan {

    public void createShaderModule(VkDevice device, VkShaderModuleCreateInfo pCreateInfo, LongBuffer pShaderModule) throws ThemisException {
        vk(
            () -> vkCreateShaderModule( device, pCreateInfo, null, pShaderModule ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void destroyShaderModule(VkDevice device, long shaderModule) throws ThemisException {
        vk( () -> vkDestroyShaderModule( device, shaderModule, null ) );
    }

    public void createGraphicsPipelines(VkDevice device, long pipelineCache, VkGraphicsPipelineCreateInfo.Buffer pCreateInfos, LongBuffer pPipelines) throws ThemisException {
        vk(
            () -> vkCreateGraphicsPipelines( device, pipelineCache, pCreateInfos, null, pPipelines ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void destroyPipeline(VkDevice device, long pipeline) throws ThemisException {
        vk( () -> vkDestroyPipeline( device, pipeline, null ) );
    }

    public void createPipelineLayout(VkDevice device, VkPipelineLayoutCreateInfo pCreateInfo, LongBuffer pPipelineLayout) throws ThemisException {
        vk(
            () -> vkCreatePipelineLayout( device, pCreateInfo, null, pPipelineLayout ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void destroyPipelineLayout(VkDevice device, long pipelineLayout) throws ThemisException {
        vk( () -> vkDestroyPipelineLayout( device, pipelineLayout, null ) );
    }

    public void createDescriptorSetLayout(VkDevice device, VkDescriptorSetLayoutCreateInfo pCreateInfo, LongBuffer pSetLayout) throws ThemisException {
        vk(
            () -> vkCreateDescriptorSetLayout( device, pCreateInfo, null, pSetLayout ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void destroyDescriptorSetLayout(VkDevice device, long descriptorSetLayout) throws ThemisException {
        vk( () -> vkDestroyDescriptorSetLayout( device, descriptorSetLayout, null ) );
    }

    public void createDescriptorPool(VkDevice device, VkDescriptorPoolCreateInfo pCreateInfo, LongBuffer pDescriptorPool) throws ThemisException {
        vk(
            () -> vkCreateDescriptorPool( device, pCreateInfo, null, pDescriptorPool ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void destroyDescriptorPool(VkDevice device, long descriptorPool) throws ThemisException {
        vk( () -> vkDestroyDescriptorPool( device, descriptorPool, null ) );
    }

    public void allocateDescriptorSets(VkDevice device, VkDescriptorSetAllocateInfo pAllocateInfo, LongBuffer pDescriptorSets) throws ThemisException {
        vk(
            () -> vkAllocateDescriptorSets( device, pAllocateInfo, pDescriptorSets ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_FRAGMENTED_POOL ) throw new VkFragmentedPoolException();
            }
        );
    }

    public void cmdBindDescriptorSets(VkCommandBuffer commandBuffer, int pipelineBindPoint, long layout, int firstSet, LongBuffer pDescriptorSets, IntBuffer pDynamicOffsets) throws ThemisException {
        vk( () -> vkCmdBindDescriptorSets( commandBuffer, pipelineBindPoint, layout, firstSet, pDescriptorSets, pDynamicOffsets ) );
    }

}
