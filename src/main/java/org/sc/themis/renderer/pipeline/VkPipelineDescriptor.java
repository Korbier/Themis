package org.sc.themis.renderer.pipeline;

import org.sc.themis.renderer.renderpass.VkRenderPass;

public record VkPipelineDescriptor(
    VkRenderPass renderPass,
    int subpass,
    boolean useBlending,
    int colorAttachmentCount,
    boolean hasDepthAttachment,
    int viewportCount,
    int scissorCount,
    int sampleCount
) {
}
