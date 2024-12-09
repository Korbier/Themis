package org.sc.themis.renderer.resource.staging;

import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.device.VkMemoryAllocator;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;

public final class VkStagingBuffer extends VkStagingResource {

    private final VkDevice device;
    private final VkMemoryAllocator allocator;
    private final int bufferUsage;
    private VkBuffer buffer;

    public VkStagingBuffer(Configuration configuration, VkStagingResourceAllocator resourceAllocator, VkDevice device, VkMemoryAllocator allocator, int bufferUsage) {
        super(configuration, resourceAllocator, device, allocator);
        this.device = device;
        this.allocator = allocator;
        this.bufferUsage = bufferUsage;
    }

    @Override
    public void doCommit(VkCommand command) throws ThemisException {
        command.copy( getStagingBuffer(), this.buffer );
    }

    @Override
    protected void setupStagingBuffer() throws ThemisException {
        super.setupStagingBuffer();
        setupFinalBuffer();
    }

    @Override
    protected void cleanupStagingBuffer() throws ThemisException {
        cleanupFinalBuffer();
        super.cleanupStagingBuffer();
    }

    public VkBuffer getBuffer() {
        return this.buffer;
    }

    private void setupFinalBuffer() throws ThemisException {
        VkBufferDescriptor vertexBufferDescriptor = new VkBufferDescriptor(getBufferSize(), VK_BUFFER_USAGE_TRANSFER_DST_BIT | this.bufferUsage, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0 );
        this.buffer = new VkBuffer( getConfiguration(), this.device, this.allocator, vertexBufferDescriptor);
        this.buffer.setup();
    }

    private void cleanupFinalBuffer() throws ThemisException {
        this.buffer.cleanup();
    }

}
