package org.sc.themis.scene.descriptorset;

import org.joml.Matrix4f;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.framebuffer.VkFrameBufferAttachment;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;

/**
 *
 *
 */
public class FramebufferAttachmentDescriptorSet extends VulkanObject {

    private final static FrameKey<VkDescriptorSet> FK_DESCRIPTORSET = FrameKey.of( VkDescriptorSet.class );

    private final Renderer renderer;
    private final VkFrameBufferAttachment [] attachments;

    private VkDescriptorSetLayout descriptorSetLayout;
    private VkDescriptorPool descriptorPool;

    public FramebufferAttachmentDescriptorSet( Configuration configuration, Renderer renderer, VkFrameBufferAttachment ... attachments ) {
        super( configuration );
        this.renderer = renderer;
        this.attachments = attachments;
    }

    public VkDescriptorSetLayout getDescriptorSetLayout() {
        return this.descriptorSetLayout;
    }

    public VkDescriptorSet getDescriptorSet( int frame ) {
        return this.renderer.getFrames().get( frame, FK_DESCRIPTORSET );
    }

    @Override
    public void setup() throws ThemisException {
        setupDescriptorLayout();
        setupDescriptorPool();
        setupDescriptorSets();
    }

    private void setupDescriptorSets() throws ThemisException {
        this.renderer.getFrames().create( FK_DESCRIPTORSET, () -> new VkDescriptorSet(getConfiguration(), this.renderer.getDevice(), this.descriptorPool, this.descriptorSetLayout ) );
        this.renderer.getFrames().update( FK_DESCRIPTORSET, desc -> {
            for ( int i = 0; i<this.attachments.length; i++ ) {
                desc.bind( i, this.attachments[i] );
            }
        } );
    }

    private void setupDescriptorLayout() throws ThemisException {

        VkDescriptorSetBinding[] bindings = new VkDescriptorSetBinding[this.attachments.length];
        for ( int i = 0; i<this.attachments.length; i++ ) {
            bindings[i] = VkDescriptorSetBinding.input( i, VK_SHADER_STAGE_FRAGMENT_BIT );
        }

        this.descriptorSetLayout = new VkDescriptorSetLayout( getConfiguration(), this.renderer.getDevice(),bindings );
        this.descriptorSetLayout.setup();

    }

    private void setupDescriptorPool() throws ThemisException {
        this.descriptorPool = new VkDescriptorPool(getConfiguration(), this.renderer.getDevice(), this.renderer.getFrames().getSize(), this.descriptorSetLayout );
        this.descriptorPool.setup();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.descriptorPool.cleanup();
        this.descriptorSetLayout.cleanup();
    }
}
