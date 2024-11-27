package org.sc.themis.renderer.base;

import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.sc.themis.renderer.exception.VkOutOfHostMemoryException;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.EXTDebugUtils.vkCreateDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT;

public class VulkanDebug extends Vulkan {

    public void createDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT pCreateInfo, LongBuffer pMessenger) throws ThemisException {
        vk(
                () -> vkCreateDebugUtilsMessengerEXT( instance, pCreateInfo, null, pMessenger ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                }
        );
    }

    public void destroyDebugUtilsMessengerEXT(VkInstance instance, long handle) throws ThemisException {
        vk( () -> vkDestroyDebugUtilsMessengerEXT( instance, handle, null ) );
    }

}
