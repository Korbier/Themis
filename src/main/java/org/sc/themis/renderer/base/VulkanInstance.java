package org.sc.themis.renderer.base;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.sc.themis.renderer.exception.*;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.IntBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkEnumerateInstanceExtensionProperties;

public class VulkanInstance extends Vulkan {

    public void createInstance(VkInstanceCreateInfo pCreateInfo, PointerBuffer pInstance ) throws ThemisException {
        vk(
                () ->  vkCreateInstance(pCreateInfo, null, pInstance),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_INITIALIZATION_FAILED ) throw new VkInitializationFailedException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_LAYER_NOT_PRESENT ) throw new VkLayerNotPresentException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_EXTENSION_NOT_PRESENT ) throw new VkExtensionNotPresentException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_INCOMPATIBLE_DRIVER ) throw new VkIncompatibleDriverException();
                }
        );
    }

    public void destroyInstance(VkInstance instance) throws VulkanException {
        vk( () -> vkDestroyInstance(instance, null) );
    }

    public void enumerateInstanceLayerProperties(IntBuffer pPropertyCount, VkLayerProperties.Buffer pProperties ) throws ThemisException {
        vk(
                () ->  vkEnumerateInstanceLayerProperties( pPropertyCount, pProperties ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void enumerateInstanceExtensionProperties(IntBuffer pPropertyCount, VkExtensionProperties.Buffer pProperties ) throws ThemisException {
        vk(
                () ->  vkEnumerateInstanceExtensionProperties( (CharSequence) null, pPropertyCount, pProperties ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_LAYER_NOT_PRESENT ) throw new VkLayerNotPresentException();
                }
        );
    }

}
