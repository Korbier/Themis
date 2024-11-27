package org.sc.themis.renderer.base;

import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.exception.VkOutOfHostMemoryException;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.EXTDebugUtils.vkCreateDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkResetFences;

public class VulkanSync extends Vulkan {

    public void createSemaphore(VkDevice device, VkSemaphoreCreateInfo pCreateInfo, LongBuffer pSemaphore) throws ThemisException {
        vk(
            () -> vkCreateSemaphore( device, pCreateInfo, null, pSemaphore),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void destroySemaphore(VkDevice device, long semaphore) throws ThemisException {
        vk( () -> vkDestroySemaphore( device, semaphore, null ) );
    }

    public void createFence(VkDevice device, VkFenceCreateInfo pCreateInfo, LongBuffer pFence) throws ThemisException {
        vk(
                    () -> vkCreateFence( device, pCreateInfo, null, pFence),
                    (errno) -> {
                        if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                        if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                    }
        );
    }

    public void destroyFence(VkDevice device, long fence) throws ThemisException {
        vk( () -> vkDestroyFence( device, fence, null ) );
    }

    public void waitForFence(VkDevice device, long fence) throws ThemisException {
        vk( () -> vkWaitForFences( device, fence, true, Long.MAX_VALUE ) );
    }

    public void resetFence(VkDevice device, long fence) throws ThemisException {
        vk(
                () -> vkResetFences( device, fence ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

}
