package org.sc.themis.renderer.renderpass;

import org.sc.themis.renderer.device.VkDevice;

import java.util.HashMap;
import java.util.Map;

public class VkSubpass {

    private final VkDevice device;
    private final int      pipelineBindPoint;

    private final Map<Integer, Integer> inputAttachments    = new HashMap<>();
    private final Map<Integer, Integer> colorAttachments    = new HashMap<>();
    private final Map<Integer, Integer> resolveAttachments  = new HashMap<>();
    private final Map<Integer, Integer> preserveAttachments = new HashMap<>();

    private int depthIndex = -1;
    private int depthLayout = -1;

    public VkSubpass(VkDevice device, int pipelineBindPoint ) {
        this.device = device;
        this.pipelineBindPoint = pipelineBindPoint;
    }

    public int getPipelineBindPoint() {
        return this.pipelineBindPoint;
    }

    public Map<Integer, Integer> inputs() {
        return this.inputAttachments;
    }

    public Map<Integer, Integer> colors() {
        return this.colorAttachments;
    }

    public Map<Integer, Integer> resolves() {
        return this.resolveAttachments;
    }

    public Map<Integer, Integer> preserves() {
        return this.preserveAttachments;
    }

    public int depthIndex() {
        return this.depthIndex;
    }

    public int depthLayout() {
        return this.depthLayout;
    }

    public VkSubpass input(int index, Integer layout ) {
        this.inputAttachments.put( index, layout );
        return this;
    }

    public VkSubpass color(int index, Integer layout ) {
        this.colorAttachments.put( index, layout );
        return this;
    }

    public VkSubpass resolve(int index, Integer layout ) {
        this.resolveAttachments.put( index, layout );
        return this;
    }

    public VkSubpass preserve(int index, Integer layout ) {
        this.preserveAttachments.put( index, layout );
        return this;
    }

    public VkSubpass depth(int index, Integer layout ) {
        this.depthIndex = index;
        this.depthLayout = layout;
        return this;
    }

}
