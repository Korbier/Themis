package org.sc.themis.renderer.base;

import org.lwjgl.PointerBuffer;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.exception.*;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.util.vma.Vma.vmaUnmapMemory;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanMemoryAllocator extends Vulkan {

    public void createAllocator(VmaAllocatorCreateInfo createInfo, PointerBuffer pAllocator) throws ThemisException {
        vk(
            () -> vmaCreateAllocator(createInfo, pAllocator),
            (errno) -> {
                //TODO Check thrown exceptions
            }
        );
    }

    public void destroyAllocator(long allocator) throws VulkanException {
        vk( () -> vmaDestroyAllocator( allocator ) );
    }

    public void createBuffer(long allocator, VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo, LongBuffer pBuffer, PointerBuffer pAllocation) throws ThemisException {
        vk(
                () -> vmaCreateBuffer(allocator, bufferCreateInfo, allocationCreateInfo, pBuffer, pAllocation, null),
                (errno) -> {
                    //TODO Check thrown exceptions
                }
        );
    }

    public void destroyBuffer(long allocator, long buffer, long allocation) throws ThemisException {
        vk( () -> vmaDestroyBuffer( allocator, buffer, allocation ) );
    }

    public void mapMemory(long allocator, long allocation, PointerBuffer pb) throws ThemisException {
        vk(
            () -> vmaMapMemory(allocator, allocation, pb),
            (errno) -> {
                //TODO Check thrown exceptions
            }
        );
    }

    public void unmapMemory(long allocator, long allocation) throws ThemisException {
        vk( () -> vmaUnmapMemory( allocator, allocation ) );
    }

    public void getImageMemoryRequirements(VkDevice device, long image, VkMemoryRequirements pMemoryRequirements) throws ThemisException {
        vk( () -> vkGetImageMemoryRequirements( device, image, pMemoryRequirements ) );
    }

    public void allocateMemory(VkDevice device, VkMemoryAllocateInfo pAllocateInfo, LongBuffer pMemory) throws ThemisException {
        vk(
            () -> vkAllocateMemory( device, pAllocateInfo, null, pMemory ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void bindImageMemory(VkDevice device, long image, long memory, long memoryOffset) throws ThemisException {
        vk(
            () -> vkBindImageMemory( device, image, memory, memoryOffset ),
            (errno) -> {
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY ) throw new VkOutOfHostMemoryException();
                if ( errno == org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY ) throw new VkOutOfDeviceMemoryException();
            }
        );
    }

    public void freeMemory(VkDevice device, long memory) throws ThemisException {
        vk( () -> vkFreeMemory( device, memory, null ) );
    }


}
