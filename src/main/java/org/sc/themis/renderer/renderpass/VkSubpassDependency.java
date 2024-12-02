package org.sc.themis.renderer.renderpass;

import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;

public record VkSubpassDependency(
    int srcSubPass,
    int dstSubPass,
    int srcStageMask,
    int dstStageMask,
    int srcAccessMask,
    int dstAccessMask,
    int flag
) {

    public VkSubpassDependency(int srcStageMask, int dstStageMask, int srcAccessMask, int dstAccessMask ) {
        this( VK_SUBPASS_EXTERNAL, 0, srcStageMask, dstStageMask, srcAccessMask,  dstAccessMask, 0 );
    }

    public VkSubpassDependency(int srcSubPass, int dstSubPass, int srcStageMask, int dstStageMask, int srcAccessMask, int dstAccessMask, int flag) {
        this.srcSubPass = srcSubPass;
        this.dstSubPass = dstSubPass;
        this.srcStageMask = srcStageMask;
        this.dstStageMask = dstStageMask;
        this.srcAccessMask = srcAccessMask;
        this.dstAccessMask = dstAccessMask;
        this.flag = flag;
    }

}