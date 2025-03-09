package org.sc.themis.renderer.base.pipeline;

import org.sc.themis.renderer.base.renderpass.VkRenderPass;

public record VkPipelineDescriptor(
    VkRenderPass renderPass,
    int subpass,
    boolean useBlending,
    int colorAttachmentCount,
    boolean hasDepthAttachment,
    int viewportCount,
    int scissorCount,
    int sampleCount) {}
