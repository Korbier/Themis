package org.sc.themis.renderer.sync;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;

public class VkSemaphore extends VulkanObject {

    private final VkDevice device;
    private long handle;

    public VkSemaphore(Configuration configuration, VkDevice device) {
        super(configuration);
        this.device = device;
    }

    @Override
    public void setup() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSemaphoreCreateInfo semaphoreCreateInfo = createSemaphoreCreateInfo(stack);
            this.handle = this.vkCreateSemaphore(stack, semaphoreCreateInfo);
        }
    }

    @Override
    public void cleanup() throws ThemisException {
        vkSync().destroySemaphore( this.device.getHandle(), this.handle );
    }

    public long getHandle() {
        return this.handle;
    }

    private long vkCreateSemaphore(MemoryStack stack, VkSemaphoreCreateInfo semaphoreCreateInfo) throws ThemisException {
        LongBuffer semaphore = stack.mallocLong(1);
        vkSync().createSemaphore( this.device.getHandle(), semaphoreCreateInfo, semaphore);
        return semaphore.get(0);
    }

    private VkSemaphoreCreateInfo createSemaphoreCreateInfo(MemoryStack stack) {
        return VkSemaphoreCreateInfo.calloc(stack).sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

    }

}
