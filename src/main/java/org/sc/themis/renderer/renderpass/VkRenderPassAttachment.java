package org.sc.themis.renderer.renderpass;

public record VkRenderPassAttachment(
        int format,
        int initialLayout,
        int finalLayout,
        int loadOp,
        int storeOp,
        int stencilLoadOp,
        int stencilStoreOp,
        int sampleCount
) {

}
