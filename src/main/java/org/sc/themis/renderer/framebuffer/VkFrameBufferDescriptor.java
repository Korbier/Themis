package org.sc.themis.renderer.framebuffer;

import org.lwjgl.vulkan.VkExtent2D;

public record VkFrameBufferDescriptor(int width, int height, int layers, long renderpass, long ... imageViews ) {

    public VkFrameBufferDescriptor(int width, int height, long renderpass, long... imageViews) {
        this( width, height, 1, renderpass, imageViews );
    }

    public VkFrameBufferDescriptor(int width, int height, int layers, long renderpass, long... imageViews) {
        this.width = width;
        this.height = height;
        this.layers = layers;
        this.renderpass = renderpass;
        this.imageViews = imageViews;
    }

    public VkFrameBufferDescriptor(VkExtent2D extent, long renderpass, long... imageViews) {
        this( extent, 1, renderpass, imageViews );
    }

    public VkFrameBufferDescriptor(VkExtent2D extent, int layers, long renderpass, long... imageViews) {
        this(extent.width(), extent.height(), layers, renderpass, imageViews);
    }

}
