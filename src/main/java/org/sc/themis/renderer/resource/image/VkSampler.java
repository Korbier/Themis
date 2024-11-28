package org.sc.themis.renderer.resource.image;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class VkSampler extends VulkanObject {

    private static final int MAX_ANISOTROPY = 16;

    private final VkDevice device;
    private final VkSamplerDescriptor descriptor;

    private long handle;

    public VkSampler(Configuration configuration, VkDevice device, VkSamplerDescriptor descriptor) {
        super(configuration);
        this.device = device;
        this.descriptor = descriptor;
    }

    @Override
    public void setup() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSamplerCreateInfo samplerCreateInfo = createSamplerCreateInfo(stack);
            this.handle = vkCreateSampler(stack, samplerCreateInfo);
        }
    }

    @Override
    public void cleanup() throws ThemisException {
        vkImage().destroySampler( this.device.getHandle(), this.handle );
    }

    private long vkCreateSampler(MemoryStack stack, VkSamplerCreateInfo samplerCreateInfo) throws ThemisException {
        LongBuffer pSampler = stack.mallocLong(1);
        vkImage().createSampler( this.device.getHandle(), samplerCreateInfo, pSampler );
        return pSampler.get(0);
    }

    private VkSamplerCreateInfo createSamplerCreateInfo(MemoryStack stack) {

        VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
        samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
        samplerInfo.magFilter( this.descriptor.filteringMode() );
        samplerInfo.minFilter( this.descriptor.filteringMode() );
        samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
        samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
        samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
        samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
        samplerInfo.unnormalizedCoordinates(false);
        samplerInfo.compareEnable(false);
        samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
        samplerInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
        samplerInfo.minLod(0.0f);
        samplerInfo.maxLod( this.descriptor.mipLevels() );
        samplerInfo.mipLodBias(0.0f);

        if (this.descriptor.anisotropyEnable() && device.isFeatureEnabled( VkDevice.FEATURE_SAMPLER_ANISOTROPY ) ) {
            samplerInfo.anisotropyEnable(true).maxAnisotropy(MAX_ANISOTROPY);
        }

        return samplerInfo;

    }

    public long getHandle() {
        return this.handle;
    }
}
