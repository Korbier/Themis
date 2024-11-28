package org.sc.themis.renderer.base;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.sc.themis.renderer.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.exception.VkOutOfHostMemoryException;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;

public class VulkanImage extends Vulkan {

    public void createImage(VkDevice device, VkImageCreateInfo pCreateInfo, LongBuffer pImage) throws ThemisException {
        vk(
            () -> vkCreateImage( device, pCreateInfo, null, pImage ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void destroyImage(VkDevice device, long image) throws ThemisException {
        vk( () -> vkDestroyImage( device, image, null ) );
    }

    public void createImageView(VkDevice device, VkImageViewCreateInfo pCreateInfo, LongBuffer pView) throws ThemisException {
        vk(
                () -> vkCreateImageView( device, pCreateInfo, null, pView ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void destroyImageView(VkDevice device, long imageView) throws ThemisException {
        vk( () -> vkDestroyImageView( device, imageView, null ) );
    }

    public void createSampler(VkDevice device, VkSamplerCreateInfo pCreateInfo, LongBuffer pSampler) throws ThemisException {
        vk(
                () -> vkCreateSampler( device, pCreateInfo, null, pSampler ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void destroySampler(VkDevice device, long sampler) throws ThemisException {
        vk( () -> vkDestroySampler( device, sampler, null ) );
    }

}
