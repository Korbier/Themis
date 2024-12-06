package org.sc.themis.renderer.pipeline.descriptorset;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;

public class VkDescriptorPool extends VulkanObject {

    private final VkDevice device;
    private final VkDescriptorSetLayout [] layouts;
    private final int ratio;
    private long handle;

    public VkDescriptorPool(Configuration configuration, VkDevice device, int ratio, VkDescriptorSetLayout ... layouts ) {
        super(configuration);
        this.device = device;
        this.ratio = ratio;
        this.layouts = layouts;
    }

    @Override
    public void setup() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Map<Integer, Integer> countersByType = countByType();
            int totalSize = countersByType.values().stream().mapToInt(i -> i).sum();
            VkDescriptorPoolSize.Buffer descriptorPoolSizes = createDescriptorPoolSizes(stack, countersByType);
            VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = createDescriptorPoolCreateInfo(stack, descriptorPoolSizes, totalSize);
            this.handle = vkCreateDescriptorPool(stack, descriptorPoolCreateInfo);
        }
    }

    @Override
    public void cleanup() throws ThemisException {
        vkPipeline().destroyDescriptorPool( this.device.getHandle(), this.handle );
    }

    public long getHandle() {
        return this.handle;
    }

    private VkDescriptorPoolCreateInfo createDescriptorPoolCreateInfo(MemoryStack stack, VkDescriptorPoolSize.Buffer descriptorPoolSizes, int totalSize ) {
        return  VkDescriptorPoolCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                .flags(VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT)
                .pPoolSizes(descriptorPoolSizes)
                .maxSets(totalSize);

    }

    private VkDescriptorPoolSize.Buffer createDescriptorPoolSizes(MemoryStack stack, Map<Integer, Integer> countersByType) {

        VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.calloc(countersByType.size(), stack);
        int idx = 0;

        for ( Integer type : countersByType.keySet() ) {
            poolSizes.get( idx++ ).type( type ).descriptorCount( countersByType.get( type ) );
        }

        return poolSizes;

    }

    private Map<Integer, Integer> countByType() {

        Map<Integer, Integer> counters = new HashMap<>();

        for ( VkDescriptorSetLayout layout : layouts ) {
            for ( int i = 0; i < layout.size(); i++ ) {

                VkDescriptorSetBinding binding = layout.getBinding( i );

                if ( counters.containsKey( binding.getDescriptorType() ) ) {
                    counters.compute( binding.getDescriptorType() , (k, value) -> value + this.ratio);
                } else {
                    counters.put( binding.getDescriptorType(), this.ratio );
                }

            }
        }

        return counters;

    }

    private long vkCreateDescriptorPool( MemoryStack stack, VkDescriptorPoolCreateInfo descriptorPoolCreateInfo ) throws ThemisException {
        LongBuffer pDescriptorPool = stack.mallocLong(1);
        vkPipeline().createDescriptorPool( this.device.getHandle(), descriptorPoolCreateInfo, pDescriptorPool);
        return pDescriptorPool.get(0);
    }

}
