package org.sc.themis.renderer.pipeline;

import java.util.ArrayList;
import java.util.List;

public class VkVertexInputStateDescriptor {

    private final int inputRate;
    private final List<Attribute> attributes = new ArrayList<>();

    public VkVertexInputStateDescriptor(int inputRate ) {
        this.inputRate = inputRate;
    }

    public VkVertexInputStateDescriptor attribute( int format, int size ) {
        this.attributes.add( new Attribute(format, size) );
        return this;
    }

    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    public int size() {
        return this.attributes.size();
    }

    public int length() {
        return getAttributes().stream().mapToInt(Attribute::size).sum();
    }

    public int getInputRate() {
        return this.inputRate;
    }

    public record Attribute(int format, int size) {}

}
