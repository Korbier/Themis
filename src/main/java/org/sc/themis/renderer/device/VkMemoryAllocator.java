package org.sc.themis.renderer.device;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public class VkMemoryAllocator extends VulkanObject {

    private final VkInstance instance;
    private final VkDevice device;
    private final VkPhysicalDevice physicalDevice;

    private long handle;

    public VkMemoryAllocator( Configuration configuration, VkPhysicalDevice physicalDevice, VkDevice device, VkInstance instance ) {
        super(configuration);
        this.physicalDevice = physicalDevice;
        this.device = device;
        this.instance = instance;
    }

    @Override
    public void setup() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            VmaAllocatorCreateInfo allocatorCreateInfo = createAllocatorCreateInfo( stack );
            this.handle = createMemoryAllcator( stack, allocatorCreateInfo );
        }
    }

    @Override
    public void cleanup() throws ThemisException {
        vkMemoryAllocator().destroyAllocator( this.handle );
    }

    public long getHandle() {
        return this.handle;
    }

    private long createMemoryAllcator(MemoryStack stack, VmaAllocatorCreateInfo allocatorCreateInfo) throws ThemisException {
        PointerBuffer pAllocator = stack.mallocPointer(1);
        vkMemoryAllocator().createAllocator( allocatorCreateInfo, pAllocator );
        return pAllocator.get(0);
    }

    private VmaAllocatorCreateInfo createAllocatorCreateInfo(MemoryStack stack) {

        VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.calloc(stack)
                .set( this.instance.getHandle(), this.device.getHandle() );

        VmaAllocatorCreateInfo createInfo = VmaAllocatorCreateInfo.calloc(stack)
                .instance(this.instance.getHandle())
                .device(this.device.getHandle())
                .physicalDevice(this.physicalDevice.getHandle())
                .pVulkanFunctions(vmaVulkanFunctions);

        return createInfo;

    }

}
