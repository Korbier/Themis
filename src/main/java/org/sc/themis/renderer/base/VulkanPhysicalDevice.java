package org.sc.themis.renderer.base;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.exception.VkInitializationFailedException;
import org.sc.themis.renderer.exception.VkLayerNotPresentException;
import org.sc.themis.renderer.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.exception.VkOutOfHostMemoryException;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.IntBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;

public class VulkanPhysicalDevice extends Vulkan {

    public void enumerateDeviceExtensionProperties(VkPhysicalDevice physicalDevice, IntBuffer pPropertyCount, VkExtensionProperties.Buffer pProperties) throws ThemisException {
        vk(
                () -> vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, pPropertyCount, pProperties),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_LAYER_NOT_PRESENT ) throw new VkLayerNotPresentException();
                }
        );
    }

    public void enumeratePhysicalDevices(VkInstance instance, IntBuffer pPhysicalDeviceCount, PointerBuffer pPhysicalDevices) throws ThemisException {
        vk(
                () ->  vkEnumeratePhysicalDevices( instance, pPhysicalDeviceCount, pPhysicalDevices ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_INITIALIZATION_FAILED ) throw new VkInitializationFailedException();
                }
        );
    }

    public void getPhysicalDeviceMemoryProperties(VkPhysicalDevice physicalDevice, VkPhysicalDeviceMemoryProperties pMemoryProperties) throws ThemisException {
        vk( () -> vkGetPhysicalDeviceMemoryProperties(physicalDevice, pMemoryProperties) );
    }

    public void getPhysicalDeviceFeatures(VkPhysicalDevice physicalDevice, VkPhysicalDeviceFeatures pFeatures) throws ThemisException {
        vk( () -> vkGetPhysicalDeviceFeatures(physicalDevice, pFeatures) );
    }

    public void getPhysicalDeviceProperties(VkPhysicalDevice physicalDevice, VkPhysicalDeviceProperties pProperties) throws ThemisException {
        vk( () -> vkGetPhysicalDeviceProperties(physicalDevice, pProperties) );
    }

    public void getPhysicalDeviceQueueFamilyProperties(VkPhysicalDevice physicalDevice, IntBuffer pQueueFamilyPropertyCount, VkQueueFamilyProperties.Buffer pQueueFamilyProperties) throws ThemisException {
        vk( () -> vkGetPhysicalDeviceQueueFamilyProperties( physicalDevice, pQueueFamilyPropertyCount, pQueueFamilyProperties) );
    }


}
