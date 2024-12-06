package org.sc.themis.renderer.pipeline.descriptorset;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;
import java.util.Arrays;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;

public class VkDescriptorSetLayout extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkDescriptorSetLayout.class);

    private final VkDevice device;
    private final VkDescriptorSetBinding [] bindings;

    private long handle;

    public VkDescriptorSetLayout(Configuration configuration, VkDevice device, VkDescriptorSetBinding ... bindings ) {
        super(configuration);
        this.device = device;
        this.bindings = bindings;
    }

    @Override
    public void setup() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = createDescriptorSetLayoutBindings(stack);
            VkDescriptorSetLayoutCreateInfo descriptorSetLayoutCreateInfo = createDescriptorSetLayoutCreateInfo(stack, descriptorSetLayoutBindings);
            this.handle = vkCreateDescriptorSetLayout(stack, descriptorSetLayoutCreateInfo);
            LOG.tracef("VkDescriptorSetLayout initialized (%s).", this);
        }
    }

    @Override
    public void cleanup() throws ThemisException {
        vkPipeline().destroyDescriptorSetLayout( this.device.getHandle(), getHandle() );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{handle=" + Long.toHexString( getHandle() ) + "}";
    }

    public VkDescriptorSetBinding getBinding( int binding ) {
        return this.bindings[binding];
    }

    public int size() {
        return this.bindings.length;
    }

    public int size( int descriptorTypeId ) {
        return (int) Arrays.stream( this.bindings ).filter( i -> i.getDescriptorType() == descriptorTypeId ).count();
    }

    public long getHandle() {
        return this.handle;
    }

    private VkDescriptorSetLayoutCreateInfo createDescriptorSetLayoutCreateInfo(MemoryStack stack, VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings) {
        return VkDescriptorSetLayoutCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                .pBindings(descriptorSetLayoutBindings);
    }

    private VkDescriptorSetLayoutBinding.Buffer createDescriptorSetLayoutBindings(MemoryStack stack) {

        VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = VkDescriptorSetLayoutBinding.calloc( this.bindings.length, stack);
        descriptorSetLayoutBindings.descriptorCount( this.bindings.length );

        for ( int i=0; i<this.bindings.length; i++ ) {
            descriptorSetLayoutBindings.get(i)
                .binding(i)
                .descriptorType(this.bindings[i].getDescriptorType())
                .stageFlags(this.bindings[i].getShaderStage())
                .descriptorCount(1);
        }

        return descriptorSetLayoutBindings;

    }

    private long vkCreateDescriptorSetLayout(MemoryStack stack, VkDescriptorSetLayoutCreateInfo descriptorSetLayoutCreateInfo) throws ThemisException {
        LongBuffer pSetLayout = stack.mallocLong(1);
        vkPipeline().createDescriptorSetLayout( this.device.getHandle(), descriptorSetLayoutCreateInfo, pSetLayout );
        return pSetLayout.get(0);
    }

}
