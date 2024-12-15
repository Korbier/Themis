package org.sc.themis.scene.descriptorset;

import org.joml.Matrix4f;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.scene.Instance;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Descriptorset layout
 *
 * MemorySizeUtils.VEC4F Selected instance identifier
 *
 * Shader source (Write)
 *
 * layout ( std140, set = X, binding = 1 ) buffer Storage {
 *   vec4 identifier;
 * } selection;
 *
 * Shader source (Read)
 *
 * layout ( std140, set = X, binding = 0 ) readonly buffer Storage {
 *   vec4 identifier;
 * } selection;
 *
 */
public class MousePickingDescriptorSet extends VulkanObject {

    private final static FrameKey<VkBuffer>        FK_BUFFER = FrameKey.of( VkBuffer.class );
    private final static FrameKey<VkDescriptorSet> FK_DESCRIPTORSET = FrameKey.of( VkDescriptorSet.class );

    private final static int BUFFER_SIZE = MemorySizeUtils.VEC4F; //Intance identifier
    private final static VkBufferDescriptor BUFFER_DESCRIPTOR = VkBufferDescriptor.descriptorsetStorageBuffer( BUFFER_SIZE );

    private final Renderer renderer;

    private VkDescriptorSetLayout descriptorSetLayout;
    private VkDescriptorPool descriptorPool;

    public MousePickingDescriptorSet(Configuration configuration, Renderer renderer ) {
        super( configuration );
        this.renderer = renderer;
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
        setupBuffers();
        setupDescriptorSets();
    }

    public float [] getSelection( int frame ) {
        VkBuffer buffer = this.renderer.getFrames().get( frame, FK_BUFFER );
        float [] identifier = new float[4];
        buffer.getMappedContent().rewind().asFloatBuffer().get( identifier );
        return identifier;
    }

    private void setupDescriptorSets() throws ThemisException {
        this.renderer.getFrames().create( FK_DESCRIPTORSET, () -> new VkDescriptorSet(getConfiguration(), this.renderer.getDevice(), this.descriptorPool, this.descriptorSetLayout ) );
        this.renderer.getFrames().update( FK_DESCRIPTORSET, (frame, descriptorset) -> descriptorset.bind( 0, this.renderer.getFrames().get( frame, FK_BUFFER ) ) );
    }

    private void setupBuffers() throws ThemisException {
        this.renderer.getFrames().create( FK_BUFFER, () -> new VkBuffer(getConfiguration(), this.renderer.getDevice(), this.renderer.getMemoryAllocator(), BUFFER_DESCRIPTOR ) );
    }

    private void setupDescriptorLayout() throws ThemisException {
        this.descriptorSetLayout = new VkDescriptorSetLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            VkDescriptorSetBinding.storageBuffer(0, VK_SHADER_STAGE_FRAGMENT_BIT)
        );
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
