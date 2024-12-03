package org.sc.themis.renderer.base;

import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.exception.*;
import org.sc.themis.shared.exception.ThemisException;

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

}
