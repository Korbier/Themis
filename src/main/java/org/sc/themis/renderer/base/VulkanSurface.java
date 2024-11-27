package org.sc.themis.renderer.base;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.exception.VkOutOfDateKHRException;
import org.sc.themis.renderer.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.exception.VkOutOfHostMemoryException;
import org.sc.themis.renderer.exception.VkSuboptimalKHRException;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class VulkanSurface extends Vulkan {

    public void createWindowSurface(VkInstance instance, long window, LongBuffer pSurface) throws ThemisException {
        vk(
                () -> GLFWVulkan.glfwCreateWindowSurface( instance, window, null, pSurface ),
                (errno) -> {
                    //TODO Check thrown exceptions
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void destroyWindowSurface(VkInstance instance, long surface) throws ThemisException {
        vk( () -> KHRSurface.vkDestroySurfaceKHR( instance, surface, null ) );
    }

    public void getPhysicalDeviceSurfaceCapabilities(VkPhysicalDevice physicalDevice, long surface, VkSurfaceCapabilitiesKHR pSurfaceCapabilities) throws ThemisException {
        vk(
                () -> KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR( physicalDevice, surface, pSurfaceCapabilities ),
                (errno) -> {
                    //TODO Check thrown exceptions
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void getPhysicalDeviceSurfaceFormatsKHR(VkPhysicalDevice physicalDevice, long surface, IntBuffer pSurfaceFormatCount, VkSurfaceFormatKHR.Buffer pSurfaceFormats) throws ThemisException {
        vk(
                () -> KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR( physicalDevice, surface, pSurfaceFormatCount, pSurfaceFormats ),
                (errno) -> {
                    //TODO Check thrown exceptions
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void createSwapchainKHR(VkDevice device, VkSwapchainCreateInfoKHR pCreateInfo, LongBuffer pSwapchain) throws ThemisException {
        vk(
                () -> KHRSwapchain.vkCreateSwapchainKHR( device, pCreateInfo, null, pSwapchain ),
                (errno) -> {
                    //TODO Check thrown exceptions
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void destroySwapchainKHR(VkDevice device, long swapchain) throws ThemisException {
        vk( () -> KHRSwapchain.vkDestroySwapchainKHR( device, swapchain, null ) );
    }

    public void getSwapchainImagesKHR(VkDevice device, long swapchain, IntBuffer pSwapchainImageCount, LongBuffer pSwapchainImages) throws ThemisException {
        vk(
                () -> KHRSwapchain.vkGetSwapchainImagesKHR( device, swapchain, pSwapchainImageCount, pSwapchainImages ),
                (errno) -> {
                    //TODO Check thrown exceptions
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void acquireNextImageKHR(VkDevice device, long swapchain, long timeout, long semaphore, long fence, IntBuffer pImageIndex) throws ThemisException {
        vk(
                () -> KHRSwapchain.vkAcquireNextImageKHR( device, swapchain, timeout, semaphore, fence, pImageIndex),
                (errno) -> {
                    //TODO Check thrown exceptions
                    if ( errno == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR ) throw new VkOutOfDateKHRException();
                    if ( errno == KHRSwapchain.VK_SUBOPTIMAL_KHR ) throw new VkSuboptimalKHRException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void queuePresentKHR(VkQueue queue, VkPresentInfoKHR pPresentInfo) throws ThemisException {
        vk(
                () -> KHRSwapchain.vkQueuePresentKHR( queue, pPresentInfo ),
                (errno) -> {
                    //TODO Check thrown exceptions
                    if ( errno == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR ) throw new VkOutOfDateKHRException();
                    if ( errno == KHRSwapchain.VK_SUBOPTIMAL_KHR ) throw new VkSuboptimalKHRException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }



}
