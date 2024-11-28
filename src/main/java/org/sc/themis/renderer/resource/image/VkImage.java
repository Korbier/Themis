package org.sc.themis.renderer.resource.image;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class VkImage extends VulkanObject {

    private final VkDevice device;
    private final VkImageDescriptor descriptor;

    private long handle;
    private long memoryHandle;

    public VkImage( Configuration configuration, VkDevice device, VkImageDescriptor descriptor ) {
        super(configuration);
        this.device = device;
        this.descriptor = descriptor;
    }

    @Override
    public void setup() throws ThemisException {
        setupImage();
        setupAllocation();
    }

    private void setupImage() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            VkImageCreateInfo imageCreateInfo = createImageCreateInfo(stack);
            this.handle = vkCreateImage(stack, imageCreateInfo);
        }
    }

    private void setupAllocation() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            VkMemoryAllocateInfo memAllocInfo = vkCreateMemoryAllocateInfo(stack);
            this.memoryHandle = vkBindImageMemory(stack, this.handle, memAllocInfo);
        }
    }

    @Override
    public void cleanup() throws ThemisException {
        vkImage().destroyImage( this.device.getHandle(), this.handle );
        vkMemoryAllocator().freeMemory( this.device.getHandle(), this.memoryHandle );
    }

    public long getHandle() {
        return this.handle;
    }

    public long getMemoryHandle() {
        return this.memoryHandle;
    }

    public VkImageDescriptor getDescriptor() {
        return this.descriptor;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {handle=" + Long.toHexString( getHandle() ) + " memory_handle=" + Long.toHexString( getMemoryHandle() ) + "}";
    }

    private long vkCreateImage(MemoryStack stack, VkImageCreateInfo imageCreateInfo) throws ThemisException {
        LongBuffer lp = stack.mallocLong(1);
        vkImage().createImage( this.device.getHandle(), imageCreateInfo, lp);
        return lp.get(0);
    }

    private VkImageCreateInfo createImageCreateInfo(MemoryStack stack) {
        return VkImageCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                .imageType(VK_IMAGE_TYPE_2D)
                .format(this.descriptor.format())
                .extent(it -> it.width( this.descriptor.width() ).height( this.descriptor.height() ).depth( 1 ) )
                .mipLevels(this.descriptor.mipLevel())
                .arrayLayers(this.descriptor.arrayLayers())
                .samples(this.descriptor.samples())
                .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                .tiling(this.descriptor.tiling())
                .usage(this.descriptor.usage());
    }

    private long vkBindImageMemory(MemoryStack stack, long image, VkMemoryAllocateInfo memAllocInfo) throws ThemisException {
        long memoryAllocation = vkAllocateMemory( stack, memAllocInfo );
        vkMemoryAllocator().bindImageMemory( this.device.getHandle(), image, memoryAllocation, 0 );
        return memoryAllocation;
    }

    private long vkAllocateMemory(MemoryStack stack, VkMemoryAllocateInfo memAllocInfo) throws ThemisException {
        LongBuffer pointer = stack.mallocLong(1);
        vkMemoryAllocator().allocateMemory( this.device.getHandle(), memAllocInfo, pointer );
        return pointer.get(0);
    }

    private VkMemoryAllocateInfo vkCreateMemoryAllocateInfo(MemoryStack stack) throws ThemisException {

        VkMemoryRequirements memReqs = VkMemoryRequirements.calloc(stack);
        vkMemoryAllocator().getImageMemoryRequirements( this.device.getHandle(), handle, memReqs );

        // Select memory size and type
        return VkMemoryAllocateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                .allocationSize(memReqs.size())
                .memoryTypeIndex( this.device.getPhysicalDevice().memoryTypeFromProperties( memReqs.memoryTypeBits(), this.descriptor.memoryTypeIndex() ) );

    }


}
