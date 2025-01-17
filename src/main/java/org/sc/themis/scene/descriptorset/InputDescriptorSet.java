package org.sc.themis.scene.descriptorset;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.framebuffer.VkFrameBufferAttachment;
import org.sc.themis.renderer.framebuffer.VkFrameBufferAttachments;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.resource.image.VkSampler;
import org.sc.themis.renderer.resource.image.VkSamplerDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class InputDescriptorSet extends VulkanObject {

    private final Renderer renderer;
    private VkFrameBufferAttachments[] inputAttachments;

    private VkDescriptorSetLayout descriptorSetLayout;
    private VkDescriptorPool descriptorPool;
    private FrameKey<VkDescriptorSet> descriptorSets = FrameKey.of( VkDescriptorSet.class );

    private org.sc.themis.renderer.resource.image.VkSampler sampler;

    public InputDescriptorSet(Configuration configuration, Renderer renderer, VkFrameBufferAttachments ... inputAttachments ) {
        super(configuration);
        this.renderer = renderer;
        this.inputAttachments = inputAttachments;
    }

    @Override
    public void setup() throws ThemisException {
        this.sampler = createDefaultSampler();
        this.descriptorSetLayout = createDescriptorSetLayout();
        this.descriptorPool = createDescriptorPool( this.descriptorSetLayout);
        createDescriptorSets( this.descriptorPool, this.descriptorSetLayout );
        this.update( this.inputAttachments );
    }

    @Override
    public void cleanup() throws ThemisException {
        this.descriptorPool.cleanup();
        this.descriptorSetLayout.cleanup();
        this.sampler.cleanup();
    }

    public VkDescriptorSetLayout getLayout() {
        return this.descriptorSetLayout;
    }

    public VkDescriptorSet getDescriptorSet( int frame ) {
        return this.renderer.getFrames().get( frame, this.descriptorSets );
    }

    public void update( VkFrameBufferAttachments ... inputAttachments  ) throws ThemisException {
        this.inputAttachments = inputAttachments;
        this.renderer.getFrames().update( this.descriptorSets, descriptorSet -> {
            int i = 0;
            for ( VkFrameBufferAttachments inputs : this.inputAttachments ) {
                for (VkFrameBufferAttachment attachment : inputs.get()) {
                    if ( attachment.getType() != VkFrameBufferAttachment.VkFrameBufferAttachmentType.RAW ) {
                        descriptorSet.bind(i++, attachment, this.sampler);
                    }
                }
            }
        });
    }

    private VkDescriptorSetLayout createDescriptorSetLayout() throws ThemisException {

        int size = 0;

        for ( VkFrameBufferAttachments inputs : this.inputAttachments ) size += (int) inputs.getImageCount();

        VkDescriptorSetBinding[] bindings = new VkDescriptorSetBinding[size];

        for ( int i=0; i<size; i++ ) {
            bindings[i] = VkDescriptorSetBinding.attachment( VK_SHADER_STAGE_FRAGMENT_BIT );
        }

        VkDescriptorSetLayout descriptorSetLayout = new VkDescriptorSetLayout( getConfiguration(), this.renderer.getDevice(), bindings );
        descriptorSetLayout.setup();

        return descriptorSetLayout;

    }

    private VkDescriptorPool createDescriptorPool(VkDescriptorSetLayout layout) throws ThemisException {
        VkDescriptorPool pool = new VkDescriptorPool( getConfiguration(), this.renderer.getDevice(), this.renderer.getFrameCount(), layout );
        pool.setup();
        return pool;
    }

    private void createDescriptorSets( VkDescriptorPool pool, VkDescriptorSetLayout layout ) throws ThemisException {
        this.renderer.getFrames().create( this.descriptorSets, () -> {
            VkDescriptorSet descriptor = new VkDescriptorSet( getConfiguration(), this.renderer.getDevice(), pool, layout );
            descriptor.setup();
            return descriptor;
        });
    }

    private VkSampler createDefaultSampler() throws ThemisException {
        org.sc.themis.renderer.resource.image.VkSampler sampler = new VkSampler( getConfiguration(), this.renderer.getDevice(), new VkSamplerDescriptor(VK_FILTER_LINEAR, 1, false ) );
        sampler.setup();
        return sampler;
    }

}
