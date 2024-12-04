package org.sc.themis.renderer.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

public class VkVertexInputState {

    private final VkVertexInputStateDescriptor [] descriptor;

    private VkVertexInputAttributeDescription.Buffer attrDescription;
    private VkVertexInputBindingDescription.Buffer bindingDescription;
    private VkPipelineVertexInputStateCreateInfo inputStateCreateInfo;

    public VkVertexInputState( VkVertexInputStateDescriptor ... descriptor ) {
        this.descriptor = descriptor;
    }

    public void setup( MemoryStack stack ) {
        this.inputStateCreateInfo = createInputStateCreateInfo( stack );
    }

    public void cleanup() {
        this.attrDescription.free();
        this.bindingDescription.free();
    }

    private VkPipelineVertexInputStateCreateInfo createInputStateCreateInfo(MemoryStack stack) {

        VkPipelineVertexInputStateCreateInfo inputStateCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc( stack );

        this.attrDescription = createAttributeDescription( stack );
        this.bindingDescription = createBindingDescription( stack );

        inputStateCreateInfo.sType( VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO )
                .pVertexBindingDescriptions( this.bindingDescription )
                .pVertexAttributeDescriptions( this.attrDescription );

        return inputStateCreateInfo;

    }

    private VkVertexInputAttributeDescription.Buffer createAttributeDescription(MemoryStack stack) {

        int count = 0;

        for ( VkVertexInputStateDescriptor descriptor : this.descriptor ) {
            count += descriptor.size();
        }

        if ( count == 0 ) return null;

        VkVertexInputAttributeDescription.Buffer attributeDescription = VkVertexInputAttributeDescription.calloc( count, stack);

        int binding = 0;
        int attrIdx = 0;

        for ( VkVertexInputStateDescriptor descriptor : this.descriptor ) {

            if (descriptor.length() > 0) {

                int offset = 0;

                for (VkVertexInputStateDescriptor.Attribute attribute : descriptor.getAttributes()) {


                    VkVertexInputAttributeDescription attr = attributeDescription.get(attrIdx);
                    int format = attribute.format();
                    int size = attribute.size();

                    attr.binding( binding ).location(attrIdx).format(format).offset(offset);

                    offset += size;
                    attrIdx++;

                }

            }

            binding++;

        }

        return attributeDescription;

    }

    private VkVertexInputBindingDescription.Buffer createBindingDescription(MemoryStack stack) {

        if ( this.descriptor.length == 0 ) return null;

        int binding = 0;
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(this.descriptor.length, stack);

        for ( VkVertexInputStateDescriptor descriptor : this.descriptor ) {
            if (descriptor.length() > 0) {
                bindingDescription.get( binding ).binding( binding ).stride( descriptor.length() ).inputRate(descriptor.getInputRate());
            }
            binding++;
        }

        return bindingDescription;

    }

    public VkPipelineVertexInputStateCreateInfo getVertexInputStateCreateInfo() {
        return this.inputStateCreateInfo;
    }

}
