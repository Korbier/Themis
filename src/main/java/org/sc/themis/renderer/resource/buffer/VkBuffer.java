package org.sc.themis.renderer.resource.buffer;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.device.VkMemoryAllocator;
import org.sc.themis.renderer.device.VkPhysicalDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class VkBuffer extends VulkanObject {

    private final VkDevice device;
    private final VkMemoryAllocator allocator;
    private final VkBufferDescriptor descriptor;

    private long handle;
    private long allocation;
    private long mappedMemoryHandle = MemoryUtil.NULL;

    private long requestedSize;
    private int  alignedSize;

    private final PointerBuffer mappingPointer = MemoryUtil.memAllocPointer(1);

    private ByteBuffer mappedContent;

    public VkBuffer(Configuration configuration, VkDevice device, VkMemoryAllocator allocator, VkBufferDescriptor descriptor) {
        super(configuration);
        this.device = device;
        this.allocator = allocator;
        this.descriptor = descriptor;
    }

    @Override
    public void setup() throws ThemisException {

        try (MemoryStack stack = MemoryStack.stackPush() ) {

            if (this.descriptor.isAligned()) {
                this.alignedSize = calcAlignedSize(this.device, this.descriptor.chunckSize());
                this.requestedSize = (long) this.alignedSize * this.descriptor.chunckCount();
            } else {
                this.alignedSize = -1;
                this.requestedSize = this.descriptor.size();
            }

            VkBufferCreateInfo bufferCreateInfo = createBufferCreateInfo(stack);
            VmaAllocationCreateInfo allocInfo = createAllocatorCreateInfo(stack);

            setupBuffer(stack, bufferCreateInfo, allocInfo);

            map();

        }

    }

    @Override
    public void cleanup() throws ThemisException {
        unmap();
        vkMemoryAllocator().destroyBuffer( this.allocator.getHandle(), this.handle, this.allocation );
    }

    private void setupBuffer(MemoryStack stack, VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocInfo) throws ThemisException {
        PointerBuffer pAllocation = stack.callocPointer(1);
        LongBuffer pBuffer = stack.mallocLong(1);
        vkMemoryAllocator().createBuffer( this.allocator.getHandle(), bufferCreateInfo, allocInfo, pBuffer, pAllocation );
        this.handle = pBuffer.get(0);
        this.allocation = pAllocation.get(0);

    }

    private VmaAllocationCreateInfo createAllocatorCreateInfo(MemoryStack stack) {
        return VmaAllocationCreateInfo.calloc(stack)
                .requiredFlags(this.descriptor.requiredFlags())
                .usage(this.descriptor.memoryUsage());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{handle=" + Long.toHexString( getHandle() )
                + " mapped_memory_handle=" + (isMapped() ? Long.toHexString( this.mappedMemoryHandle ) : "NULL" )
                + "}";
    }

    public long getHandle() {
        return this.handle;
    }

    public long getRequestedSize() {
        return this.requestedSize;
    }

    public int getAlignedSize() {
        return this.alignedSize;
    }

    public boolean isAligned() {
        return this.descriptor.isAligned();
    }

    public void map() throws ThemisException {
        if ( isMappable() ) {
            vkMemoryAllocator().mapMemory( this.allocator.getHandle(), this.allocation, this.mappingPointer );
            this.mappedMemoryHandle = this.mappingPointer.get(0);
            this.mappedContent = MemoryUtil.memByteBuffer( this.mappedMemoryHandle, (int) getRequestedSize() );
        }
    }

    public void unmap() throws ThemisException {
        if ( isMapped() ) {
            this.mappedContent = null;
            vkMemoryAllocator().unmapMemory( this.allocator.getHandle(), this.allocation );
        }
    }

    public boolean isMappable() {
        return (this.descriptor.memoryUsage() & VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) != 0;
    }

    public boolean isMapped() {
        return this.mappedMemoryHandle != MemoryUtil.NULL;
    }

    public void set( ByteBuffer data ) {
        if ( isMapped() ) this.mappedContent.put( data );
    }

    public void set( int offset, Vector3f value ) {
        if ( isMapped() ) value.get( offset, this.mappedContent );
    }

    public void set( int offset, Vector4f value ) {
        if ( isMapped() ) value.get( offset, this.mappedContent );
    }

    public void set( int offset, Matrix4f value ) {
        if ( isMapped() ) value.get( offset, this.mappedContent );
    }

    public void set( int offset, Vector2i value ) {
        if ( isMapped() ) value.get( offset, this.mappedContent );
    }

    public void set( int offset, float value ) {
        if ( isMapped() ) this.mappedContent.putFloat( offset, value );
    }

    public void set( int offset, int value ) {
        if ( isMapped() ) this.mappedContent.putInt( offset, value );
    }

    public void set( int offset, float [] values ) {
        if ( isMapped() ) {
            int o = offset;
            for ( float f : values ) {
                this.mappedContent.putFloat(o, f);
                o += MemorySizeUtils.FLOAT;
            }
        }
    }

    private VkBufferCreateInfo createBufferCreateInfo(MemoryStack stack) {
        return VkBufferCreateInfo.calloc(stack)
            .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
            .size(this.getRequestedSize())
            .usage(this.descriptor.bufferUsage())
            .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
    }

    private int calcAlignedSize( VkDevice device, long originalSize ) {

        VkPhysicalDevice physDevice = device.getPhysicalDevice();
        long minUboAlignment = physDevice.getVkPhysicalDeviceProperties().limits().minUniformBufferOffsetAlignment();
        long alignedSize     = originalSize;

        if ( minUboAlignment > 0 ) {
            alignedSize = ((originalSize / minUboAlignment) + 1) * minUboAlignment;
        }

        return (int) alignedSize;

    }

}
