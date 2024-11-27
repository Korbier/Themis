package org.sc.themis.renderer.base;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.sc.themis.renderer.exception.VkOutOfDeviceMemoryException;
import org.sc.themis.renderer.exception.VkOutOfHostMemoryException;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;

public class VulkanImage extends Vulkan {

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

}
