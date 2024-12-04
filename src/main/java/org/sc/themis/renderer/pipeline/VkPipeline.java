package org.sc.themis.renderer.pipeline;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

public class VkPipeline extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkPipeline.class);

    private final VkDevice device;
    private final VkPipelineDescriptor descriptor;
    private final VkShaderProgram shaderProgram;
    private final VkPipelineLayout layout;
    private final VkVertexInputState vertexInputState;

    private long handle;

    public VkPipeline(
        Configuration configuration,
        VkDevice device,
        VkPipelineDescriptor descriptor,
        VkShaderProgram shaderProgram,
        VkPipelineLayout layout,
        VkVertexInputState vertexInputState
    ) {
        super(configuration);
        this.device = device;
        this.descriptor = descriptor;
        this.shaderProgram = shaderProgram;
        this.layout = layout;
        this.vertexInputState = vertexInputState;
    }

    @Override
    public void setup() throws ThemisException {

        try ( MemoryStack stack = MemoryStack.stackPush() ) {

            VkPipelineShaderStageCreateInfo.Buffer shaderStageCreateInfo = createShaderStageCreateInfo(stack);
            VkPipelineInputAssemblyStateCreateInfo inputAssemblyStageCreateInfo = createInputAssemblyStateCreateInfo(stack);
            VkPipelineViewportStateCreateInfo viewportStateCreateInfo = createViewportStateCreateInfo(stack);
            VkPipelineRasterizationStateCreateInfo rasterizationStateCreateInfo = createRasterizationStateCreateInfo(stack);
            VkPipelineMultisampleStateCreateInfo multisamplingStateCreateInfo = createMultisamplingStateCreateInfo(stack);
            VkPipelineColorBlendStateCreateInfo colorBlendStateCreateInfo = createColorBlendStateCreateInfo(stack);
            VkPipelineDepthStencilStateCreateInfo depthStencilStateCreateInfo = createDepthStencilStateCreateInfo(stack);
            VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = createDynamicStateCreateInfo(stack);

            VkGraphicsPipelineCreateInfo.Buffer graphicsPipelineCreateInfo = createGraphicPipelineCreateInfo(
                    stack,
                    shaderStageCreateInfo,
                    inputAssemblyStageCreateInfo,
                    viewportStateCreateInfo,
                    rasterizationStateCreateInfo,
                    multisamplingStateCreateInfo,
                    colorBlendStateCreateInfo,
                    depthStencilStateCreateInfo,
                    dynamicStateCreateInfo
            );

            this.handle = vkCreateGraphicPipeline(stack, graphicsPipelineCreateInfo);

            LOG.trace("Pipeline initialized");

        }
    }

    @Override
    public void cleanup() throws ThemisException {
        vkPipeline().destroyPipeline( this.device.getHandle(), this.getHandle() );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {handle=" + Long.toHexString( getHandle() ) + "}";
    }

    public Long getHandle() {
        return this.handle;
    }

    public VkPipelineLayout getPipelineLayout() {
        return this.layout;
    }

    private VkGraphicsPipelineCreateInfo.Buffer createGraphicPipelineCreateInfo(
            MemoryStack stack, VkPipelineShaderStageCreateInfo.Buffer shaderStageCreateInfo,
            VkPipelineInputAssemblyStateCreateInfo inputAssemblyStageCreateInfo,
            VkPipelineViewportStateCreateInfo viewportStateCreateInfo,
            VkPipelineRasterizationStateCreateInfo rasterizationStateCreateInfo,
            VkPipelineMultisampleStateCreateInfo multisamplingStateCreateInfo,
            VkPipelineColorBlendStateCreateInfo colorBlendStateCreateInfo,
            VkPipelineDepthStencilStateCreateInfo depthStencilStateCreateInfo,
            VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo
    ) {

        VkGraphicsPipelineCreateInfo.Buffer graphicPipelineCreateInfo =  VkGraphicsPipelineCreateInfo.calloc(1, stack)
                .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                .pStages( shaderStageCreateInfo )
                .pVertexInputState( this.vertexInputState.getVertexInputStateCreateInfo() )
                .pInputAssemblyState( inputAssemblyStageCreateInfo )
                .pViewportState( viewportStateCreateInfo )
                .pRasterizationState( rasterizationStateCreateInfo )
                .pMultisampleState( multisamplingStateCreateInfo )
                .pColorBlendState( colorBlendStateCreateInfo )
                .pDynamicState( dynamicStateCreateInfo )
                .renderPass( this.descriptor.renderPass().getHandle() )
                .subpass( this.descriptor.subpass() )
                .layout( this.layout.getHandle() );


        if ( depthStencilStateCreateInfo != null ) {
            graphicPipelineCreateInfo.pDepthStencilState( depthStencilStateCreateInfo );
        }

        return graphicPipelineCreateInfo;

    }

    private VkPipelineDynamicStateCreateInfo createDynamicStateCreateInfo(MemoryStack stack) {
        return VkPipelineDynamicStateCreateInfo.calloc(stack)
            .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
            .pDynamicStates(stack.ints( VK_DYNAMIC_STATE_VIEWPORT,VK_DYNAMIC_STATE_SCISSOR ));
    }

    private VkPipelineDepthStencilStateCreateInfo createDepthStencilStateCreateInfo(MemoryStack stack) {

        if ( this.descriptor.hasDepthAttachment() ) {

            return VkPipelineDepthStencilStateCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
                    .depthTestEnable(true)
                    .depthWriteEnable(true)
                    .depthCompareOp(VK_COMPARE_OP_LESS_OR_EQUAL)
                    .depthBoundsTestEnable(false)
                    .stencilTestEnable(false);

        } else {

            return null;

        }

    }


    private VkPipelineShaderStageCreateInfo.Buffer createShaderStageCreateInfo(MemoryStack stack) {

        VkPipelineShaderStageCreateInfo.Buffer shaderStageCreateInfo = VkPipelineShaderStageCreateInfo.calloc( this.shaderProgram.size(), stack);

        int i = 0;
        for ( Map.Entry<Integer, Long> entry : this.shaderProgram.handles().entrySet() ) {
            shaderStageCreateInfo.get(i)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(entry.getKey())
                    .module(entry.getValue())
                    .pName(stack.UTF8("main"));
            i++;
        }

        return shaderStageCreateInfo;

    }

    private VkPipelineInputAssemblyStateCreateInfo createInputAssemblyStateCreateInfo(MemoryStack stack) {
        return VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
    }

    private VkPipelineViewportStateCreateInfo createViewportStateCreateInfo(MemoryStack stack) {
        return VkPipelineViewportStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                .viewportCount( this.descriptor.viewportCount() )
                .scissorCount( this.descriptor.scissorCount() );
    }

    private VkPipelineRasterizationStateCreateInfo createRasterizationStateCreateInfo(MemoryStack stack) {
        return VkPipelineRasterizationStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                .polygonMode(VK_POLYGON_MODE_FILL)
                .cullMode(VK_CULL_MODE_NONE) //VK_CULL_MODE_NONE
                .frontFace(VK_FRONT_FACE_CLOCKWISE) //VK_FRONT_FACE_CLOCKWISE
                .lineWidth(1.0f);
    }

    private VkPipelineMultisampleStateCreateInfo createMultisamplingStateCreateInfo(MemoryStack stack) {
        return VkPipelineMultisampleStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                .rasterizationSamples(this.descriptor.sampleCount());
    }

    private VkPipelineColorBlendStateCreateInfo createColorBlendStateCreateInfo(MemoryStack stack) {

        int colorAttachmentCount = this.descriptor.colorAttachmentCount();
        VkPipelineColorBlendAttachmentState.Buffer blendAttachmentState = VkPipelineColorBlendAttachmentState.calloc(colorAttachmentCount, stack);

        for (int i=0; i<colorAttachmentCount; i++) {

            blendAttachmentState.get(i)
                    .colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT)
                    .blendEnable( this.descriptor.useBlending() );

            if ( this.descriptor.useBlending() ) {
                blendAttachmentState.get(i).colorBlendOp(VK_BLEND_OP_ADD)
                    .alphaBlendOp(VK_BLEND_OP_ADD)
                    .srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA)
                    .dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
                    .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
                    .dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO);
            }

        }

        return VkPipelineColorBlendStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                .pAttachments(blendAttachmentState);

    }

    private long vkCreateGraphicPipeline(MemoryStack stack, VkGraphicsPipelineCreateInfo.Buffer graphicsPipelineCreateInfo) throws ThemisException {
        LongBuffer lp = stack.mallocLong(1);
        vkPipeline().createGraphicsPipelines( this.device.getHandle(), VK_NULL_HANDLE, graphicsPipelineCreateInfo, lp );
        return lp.get( 0 );
    }

}
