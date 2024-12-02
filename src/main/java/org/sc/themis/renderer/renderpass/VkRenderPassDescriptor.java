package org.sc.themis.renderer.renderpass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;

public class VkRenderPassDescriptor {

    private final VkRenderPassLayout layout;
    private final List<VkSubpass> subpasses = new ArrayList<>();
    private final List<VkSubpassDependency> dependencies = new ArrayList<>();

    public VkRenderPassDescriptor(VkRenderPassLayout layout ) {
        this.layout = layout;
    }

    public VkRenderPassLayout getLayout() {
        return this.layout;
    }

    public List<VkSubpass> getSubpasses() {
        return this.subpasses;
    }

    public List<VkSubpassDependency> getDependencies() {
        return this.dependencies;
    }

    public VkRenderPassDescriptor subpass(VkSubpass ... subpass) {
        this.subpasses.addAll( Arrays.asList( subpass ) );
        return this;
    }

    public VkRenderPassDescriptor dependency(VkSubpassDependency dependency ) {
        this.dependencies.add( dependency );
        return this;
    }

    public VkRenderPassDescriptor dependency(int srcSubPass, int dstSubPass, int srcStageMask, int dstStageMask, int srcAccessMask, int dstAccessMask, Integer flag ) {
        this.dependencies.add( new VkSubpassDependency(srcSubPass, dstSubPass, srcStageMask, dstStageMask, srcAccessMask, dstAccessMask, flag ) );
        return this;
    }

    public VkRenderPassDescriptor dependency(int dstSubPass, int srcStageMask, int dstStageMask, int srcAccessMask, int dstAccessMask, Integer flag ) {
        this.dependencies.add( new VkSubpassDependency(VK_SUBPASS_EXTERNAL, dstSubPass, srcStageMask, dstStageMask, srcAccessMask, dstAccessMask, flag ) );
        return this;
    }

}
