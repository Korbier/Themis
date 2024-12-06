package org.sc.themis.renderer.pipeline.descriptorset;

import static org.lwjgl.vulkan.VK10.*;

public class VkDescriptorSetBinding {

    private int binding = 0;
    private int descriptorType = -1;
    private int shaderStage = -1;

    public static VkDescriptorSetBinding input( int binding, int shaderStage ) {
        VkDescriptorSetBinding descriptorSetBinding = new VkDescriptorSetBinding();
        descriptorSetBinding.binding = binding;
        descriptorSetBinding.descriptorType = VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT;
        descriptorSetBinding.shaderStage = shaderStage;
        return descriptorSetBinding;
    }

    public static VkDescriptorSetBinding uniform( int binding, int shaderStage ) {
        VkDescriptorSetBinding descriptorSetBinding = new VkDescriptorSetBinding();
        descriptorSetBinding.binding = binding;
        descriptorSetBinding.descriptorType = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
        descriptorSetBinding.shaderStage = shaderStage;
        return descriptorSetBinding;
    }

    public static VkDescriptorSetBinding storageBuffer( int binding, int shaderStage ) {
        VkDescriptorSetBinding descriptorSetBinding = new VkDescriptorSetBinding();
        descriptorSetBinding.binding = binding;
        descriptorSetBinding.descriptorType = VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
        descriptorSetBinding.shaderStage = shaderStage;
        return descriptorSetBinding;
    }

    public static VkDescriptorSetBinding dynamicUniform( int binding, int shaderStage ) {
        VkDescriptorSetBinding descriptorSetBinding = new VkDescriptorSetBinding();
        descriptorSetBinding.binding = binding;
        descriptorSetBinding.descriptorType = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;
        descriptorSetBinding.shaderStage = shaderStage;
        return descriptorSetBinding;
    }

    public static VkDescriptorSetBinding combinedImageSampler( int binding, int shaderStage ) {
        VkDescriptorSetBinding descriptorSetBinding = new VkDescriptorSetBinding();
        descriptorSetBinding.binding = binding;
        descriptorSetBinding.descriptorType = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        descriptorSetBinding.shaderStage = shaderStage;
        return descriptorSetBinding;
    }

    private VkDescriptorSetBinding() { }

    public int getBinding() {
        return this.binding;
    }

    public int getDescriptorType() {
        return descriptorType;
    }

    public int getShaderStage() {
        return shaderStage;
    }

}
