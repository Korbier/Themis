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

}
