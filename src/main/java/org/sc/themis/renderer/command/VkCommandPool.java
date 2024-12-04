package org.sc.themis.renderer.command;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.queue.VkQueue;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;

public class VkCommandPool extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkCommandPool.class);

    private final VkDevice device;
    private final VkQueue queue;
    private long handle;

    public VkCommandPool( Configuration configuration, VkDevice device, VkQueue queue ) {
        super(configuration);
        this.device = device;
        this.queue = queue;
    }

    @Override
    public void setup() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.handle = this.vkCreateCommandPool(stack);
        }
        LOG.trace("CommandPool initialized.");
    }

    @Override
    public void cleanup() throws ThemisException {
        vkDestroyCommandPool();
    }

    public long getHandle() {
        return this.handle;
    }

    public VkCommand create( boolean primary ) throws ThemisException {

        VkCommandBuffer buffer = new VkCommandBuffer(getConfiguration(), this.device, this, this.queue, primary );
        buffer.setup();

        return new VkCommand( getConfiguration(), buffer );

    }

    private long vkCreateCommandPool(MemoryStack stack) throws ThemisException {

        VkCommandPoolCreateInfo cmdPoolInfo = VkCommandPoolCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                .queueFamilyIndex(this.queue.getQueueFamilyIndex());

        LongBuffer lp = stack.mallocLong(1);

        vkCommand().createCommandPool( this.device.getHandle(), cmdPoolInfo, lp );

        return lp.get(0);

    }

    private void vkDestroyCommandPool() throws ThemisException {
        vkCommand().destroyCommandPool( this.device.getHandle(), this.handle );
    }

}
