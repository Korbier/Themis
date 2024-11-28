package org.sc.themis.renderer.resource.buffer;

public record VkBufferDescriptor (
        boolean isAligned,
        long size, //the size in bytes of the buffer to be created.
        long chunckSize,
        int chunckCount,
        int bufferUsage, //a bitmask of VkBufferUsageFlagBits specifying allowed usages of the buffer.
        int memoryUsage,
        int requiredFlags //an index identifying a memory type from the memoryTypes array of the VkPhysicalDeviceMemoryProperties structure.
) {

    public VkBufferDescriptor( long size, int bufferUsage, int memoryUsage, int requiredFlags) {
        this( false, size, -1, -1, bufferUsage, memoryUsage, requiredFlags );
    }

    public VkBufferDescriptor( long chunckSize, int chunckCount, int bufferUsage, int memoryUsage, int requiredFlags) {
        this( true, -1, chunckSize, chunckCount, bufferUsage, memoryUsage, requiredFlags );
    }

}
