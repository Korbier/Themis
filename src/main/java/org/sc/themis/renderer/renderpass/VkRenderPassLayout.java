package org.sc.themis.renderer.renderpass;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;

public class VkRenderPassLayout {

    private final Map<Integer, VkRenderPassAttachment> attachments = new HashMap<>();

    public int size() {
        return this.attachments.size();
    }

    public VkRenderPassLayout add(int index, VkRenderPassAttachment attachment) {
        this.attachments.put(index, attachment);
        return this;
    }

    public VkRenderPassLayout add(int index, int format, int initialLayout, int finalLayout, int loadOp, int storeOp, int stencilLoadOp, int stencilStoreOp, int sampleCount) {
        add( index, new VkRenderPassAttachment(format, initialLayout, finalLayout, loadOp, storeOp, stencilLoadOp, stencilStoreOp, sampleCount) );
        return this;
    }

    public VkRenderPassLayout add(int index, int format, int initialLayout, int finalLayout, int loadOp, int storeOp, int stencilLoadOp, int stencilStoreOp) {
        add( index, new VkRenderPassAttachment(format, initialLayout, finalLayout, loadOp, storeOp, stencilLoadOp, stencilStoreOp, VK_SAMPLE_COUNT_1_BIT) );
        return this;
    }

    public VkRenderPassAttachment get( int index ) {
        return this.attachments.get( index );
    }

    public Set<Integer> keys() {
        return this.attachments.keySet();
    }

}
