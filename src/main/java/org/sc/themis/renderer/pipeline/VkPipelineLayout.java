package org.sc.themis.renderer.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;

public class VkPipelineLayout extends VulkanObject {

    private final VkDevice device;
    private final VkPushConstantRange [] constantRanges;
    private final VkDescriptorSetLayout [] descriptorSetLayouts;

    private long handle;

    public VkPipelineLayout(Configuration configuration, VkDevice device, VkPushConstantRange[] pushConstantRanges, VkDescriptorSetLayout ... descriptorSetLayouts ) {
        super(configuration);
        this.device = device;
        this.constantRanges = pushConstantRanges;
        this.descriptorSetLayouts = descriptorSetLayouts;
    }

    @Override
    public void setup() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            org.lwjgl.vulkan.VkPushConstantRange.Buffer pushConstantRanges = createPushConstantRanges(stack);
            LongBuffer layouts = createLayouts(stack);
            VkPipelineLayoutCreateInfo layoutCreateInfo = createLayoutCreateInfo(stack, pushConstantRanges, layouts);
            this.handle = vkCreatePipelineLayout(stack, layoutCreateInfo);
        }
    }

    @Override
    public void cleanup() throws ThemisException {
        vkPipeline().destroyPipelineLayout( this.device.getHandle(), this.handle );
    }

    public long getHandle() {
        return this.handle;
    }

    private LongBuffer createLayouts(MemoryStack stack) {

        if ( this.descriptorSetLayouts.length > 0 ) {

            LongBuffer layouts = stack.mallocLong(this.descriptorSetLayouts.length);

            for (int i = 0; i < this.descriptorSetLayouts.length; i++) {
                layouts.put(i, this.descriptorSetLayouts[i].getHandle());
            }

            return layouts;

        }

        return null;

    }

    private VkPipelineLayoutCreateInfo createLayoutCreateInfo(MemoryStack stack, org.lwjgl.vulkan.VkPushConstantRange.Buffer pushConstantRanges, LongBuffer layouts ) {

        VkPipelineLayoutCreateInfo pPipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc(stack).sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);

        if ( layouts != null ) pPipelineLayoutCreateInfo.pSetLayouts( layouts );
        if ( pushConstantRanges != null ) pPipelineLayoutCreateInfo.pPushConstantRanges( pushConstantRanges );

        return pPipelineLayoutCreateInfo;

    }

    private org.lwjgl.vulkan.VkPushConstantRange.Buffer createPushConstantRanges(MemoryStack stack) {

        if ( this.constantRanges.length > 0 ) {

            org.lwjgl.vulkan.VkPushConstantRange.Buffer pushConstantRanges = org.lwjgl.vulkan.VkPushConstantRange.calloc( this.constantRanges.length , stack );

            for ( int i=0; i<this.constantRanges.length; i++) {
                VkPushConstantRange range = this.constantRanges[i];
                pushConstantRanges.get( i )
                        .stageFlags( range.stage() )
                        .offset( range.offset() )
                        .size( range.size() );
            }

            return pushConstantRanges;

        }

        return null;
    }

    private long vkCreatePipelineLayout( MemoryStack stack, VkPipelineLayoutCreateInfo layoutCreateInfo ) throws ThemisException {
        LongBuffer lp = stack.mallocLong(1);
        vkPipeline().createPipelineLayout( this.device.getHandle(), layoutCreateInfo, lp );
        return lp.get( 0 );
    }

}
