package org.sc.themis.renderer.base;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.exception.*;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.ConsumerWithException;
import org.sc.themis.shared.tobject.TObject;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.function.Supplier;

import static org.lwjgl.vulkan.EXTDebugUtils.vkCreateDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkEnumerateInstanceLayerProperties;

public abstract class VkObject extends TObject {

    public VkObject(Configuration configuration) {
        super( configuration );
    }

    protected void createDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT pCreateInfo, LongBuffer pMessenger) throws ThemisException {
        vk(
            () -> vkCreateDebugUtilsMessengerEXT( instance, pCreateInfo, null, pMessenger ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
            }
        );
    }

    protected void destroyDebugUtilsMessengerEXT(VkInstance instance, long handle) throws ThemisException {
        vk( () -> vkDestroyDebugUtilsMessengerEXT( instance, handle, null ) );
    }

    protected void createInstance(VkInstanceCreateInfo pCreateInfo, PointerBuffer pInstance ) throws ThemisException {
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

    protected void destroyInstance(VkInstance instance) throws VulkanException {
        vk( () -> vkDestroyInstance(instance, null) );
    }

    protected void enumerateInstanceLayerProperties(IntBuffer pPropertyCount, VkLayerProperties.Buffer pProperties ) throws ThemisException {
        vk(
                () ->  vkEnumerateInstanceLayerProperties( pPropertyCount, pProperties ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    protected void enumerateInstanceExtensionProperties(IntBuffer pPropertyCount, VkExtensionProperties.Buffer pProperties ) throws ThemisException {
        vk(
            () ->  vkEnumerateInstanceExtensionProperties( (CharSequence) null, pPropertyCount, pProperties ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_LAYER_NOT_PRESENT ) throw new VkLayerNotPresentException();
            }
        );
    }

    protected void enumerateDeviceExtensionProperties(VkPhysicalDevice physicalDevice, IntBuffer pPropertyCount, VkExtensionProperties.Buffer pProperties) throws ThemisException {
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

    protected void getPhysicalDeviceMemoryProperties(VkPhysicalDevice physicalDevice, VkPhysicalDeviceMemoryProperties pMemoryProperties) throws ThemisException {
        vk( () -> vkGetPhysicalDeviceMemoryProperties(physicalDevice, pMemoryProperties) );
    }

    protected void getPhysicalDeviceFeatures(VkPhysicalDevice physicalDevice, VkPhysicalDeviceFeatures pFeatures) throws ThemisException {
        vk( () -> vkGetPhysicalDeviceFeatures(physicalDevice, pFeatures) );
    }

    protected void getPhysicalDeviceProperties(VkPhysicalDevice physicalDevice, VkPhysicalDeviceProperties pProperties) throws ThemisException {
        vk( () -> vkGetPhysicalDeviceProperties(physicalDevice, pProperties) );
    }

    protected void getPhysicalDeviceQueueFamilyProperties(VkPhysicalDevice physicalDevice, IntBuffer pQueueFamilyPropertyCount, VkQueueFamilyProperties.Buffer pQueueFamilyProperties) throws ThemisException {
        vk( () -> vkGetPhysicalDeviceQueueFamilyProperties( physicalDevice, pQueueFamilyPropertyCount, pQueueFamilyProperties) );
    }

    /**
     * Executes a Vulkan operation using the provided supplier and handles errors with the given consumer.
     *
     * @param supplier               the supplier for obtaining the Vulkan operation result
     * @param consumerWithException    the consumer for handling Vulkan errors
     * @throws ThemisException       if an unknown error occurs during the Vulkan operation
     */
    protected void vk(Supplier<Integer> supplier, ConsumerWithException<Integer> consumerWithException) throws ThemisException {

        int errno = supplier.get();

        if ( errno == VK10.VK_SUCCESS ) {
            return;
        }

        consumerWithException.accept( errno );

        throw new VulkanException(errno, "Unknown error");

    }

    /**
     * Executes a Vulkan operation using the provided callable and handles errors.
     *
     * @param executor         the executor for executing the Vulkan operation
     * @throws VulkanException if an unknown error occurs during the Vulkan operation
     */
    protected void vk(VulkanExecutor executor) throws VulkanException {
        try {
            executor.execute();
        } catch (Exception exception) {
            throw new VulkanException("Unknown error", exception);
        }
    }

    protected interface VulkanExecutor {
        void execute();
    }


}
