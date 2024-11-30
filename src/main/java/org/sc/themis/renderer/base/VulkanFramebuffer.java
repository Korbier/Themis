package org.sc.themis.renderer.base;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.exception.*;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanFramebuffer extends Vulkan {

    public void createFramebuffer(VkDevice device, VkFramebufferCreateInfo pCreateInfo, LongBuffer pFramebuffer) throws ThemisException {
        vk(
                () -> vkCreateFramebuffer( device, pCreateInfo, null, pFramebuffer ),
                (errno) -> {
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                    if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
                }
        );
    }

    public void destroyFramebuffer(VkDevice device, long framebuffer) throws ThemisException {
        vk( () -> vkDestroyFramebuffer( device, framebuffer, null ) );
    }


}
