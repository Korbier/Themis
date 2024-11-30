package org.sc.themis.renderer.base;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.sc.themis.renderer.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.exception.VkOutOfHostMemoryException;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkDestroyRenderPass;

public class VulkanRenderPass extends Vulkan {

    public void createRenderPass(VkDevice device, VkRenderPassCreateInfo pCreateInfo, LongBuffer pRenderPass) throws ThemisException {
        vk(
                () -> vkCreateRenderPass( device, pCreateInfo, null, pRenderPass ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void destroyRenderPass(VkDevice device, long renderPass) throws ThemisException {
        vk( () -> vkDestroyRenderPass( device, renderPass, null ) );
    }



}
