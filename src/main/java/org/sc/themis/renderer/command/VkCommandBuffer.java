package org.sc.themis.renderer.command;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.queue.VkQueue;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import static org.lwjgl.vulkan.VK13.*;

public class VkCommandBuffer extends VulkanObject {

    private final VkDevice device;
    private final VkCommandPool commandPool;
    private final VkQueue queue;
    private final boolean primary;

    private org.lwjgl.vulkan.VkCommandBuffer handle;

    public VkCommandBuffer(Configuration configuration, VkDevice device, VkCommandPool pool, VkQueue queue, boolean primary ) {
        super(configuration);
        this.device = device;
        this.commandPool = pool;
        this.queue = queue;
        this.primary = primary;
    }

    @Override
    public void setup() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.handle = vkAllocateCommandBuffer(stack);
        }
    }

    @Override
    public void cleanup() throws ThemisException {
        vkCommand().freeCommandBuffers( this.device.getHandle(), this.commandPool.getHandle(), this.handle );
    }

    public org.lwjgl.vulkan.VkCommandBuffer getHandle() {
        return this.handle;
    }

    public VkQueue getQueue() {
        return this.queue;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    private org.lwjgl.vulkan.VkCommandBuffer vkAllocateCommandBuffer(MemoryStack stack) throws ThemisException {

        VkCommandBufferAllocateInfo cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .commandPool(this.commandPool.getHandle())
                .level(primary ? VK_COMMAND_BUFFER_LEVEL_PRIMARY : VK_COMMAND_BUFFER_LEVEL_SECONDARY)
                .commandBufferCount(1);

        PointerBuffer buffer = stack.mallocPointer(1);

        vkCommand().allocateCommandBuffers(this.device.getHandle(), cmdBufAllocateInfo, buffer);

        return new org.lwjgl.vulkan.VkCommandBuffer( buffer.get(0), this.device.getHandle());

    }

}
