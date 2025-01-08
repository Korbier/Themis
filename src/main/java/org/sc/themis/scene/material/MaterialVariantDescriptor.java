package org.sc.themis.scene.material;

import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.resource.image.VkSamplerDescriptor;

import java.util.HashMap;
import java.util.Map;

public class MaterialVariantDescriptor {

    private final Map<Integer, VkDescriptorSetBinding> bindings = new HashMap<>();
    private final Map<Integer, VkBufferDescriptor> bufferDescriptors = new HashMap<>();
    private final Map<Integer, VkSamplerDescriptor> samplerBufferDescriptors = new HashMap<>();

    public void addUniformBinding( int binding, int shaderStage, VkBufferDescriptor bufferDescriptor ) {
        this.bindings.put( binding, VkDescriptorSetBinding.uniform( binding, shaderStage) );
        this.bufferDescriptors.put( binding, bufferDescriptor );
    }

    public void addCombinedImageSamplerBinding(int binding, int shaderStage, VkSamplerDescriptor samplerDescriptor) {
        this.bindings.put( binding, VkDescriptorSetBinding.combinedImageSampler( binding, shaderStage) );
        this.samplerBufferDescriptors.put( binding, samplerDescriptor );
    }

    public Map<Integer, VkDescriptorSetBinding> getBindings() {
        return this.bindings;
    }

    public VkBufferDescriptor getBufferDescriptor( int binding ) {

        if ( this.bufferDescriptors.containsKey( binding ) ) {
            return this.bufferDescriptors.get( binding );
        }

        return null;

    }

    public VkSamplerDescriptor getSamplerDescriptor( int binding ) {

        if ( this.samplerBufferDescriptors.containsKey( binding ) ) {
            return this.samplerBufferDescriptors.get( binding );
        }

        return null;

    }

}
