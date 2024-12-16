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
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;

/**
 * Descriptorset layout
 *
 * MemorySizeUtils.MAT4x4F Projection
 * MemorySizeUtils.MAT4x4F View
 * MemorySizeUtils.MAT4x4F Inverse projection
 * MemorySizeUtils.MAT4x4F Inverse view
 * MemorySizeUtils.MAT4x4F Camera position
 * MemorySizeUtils.VEC2F   Resolution
 * MemorySizeUtils.INT     UTime
 *
 * Shader source
 * layout(std140, set = 0, binding = 0) uniform Global {
 *     mat4 projection;
 *     mat4 view;
 *     mat4 projectionInv;
 *     mat4 viewInv;
 *     vec4 camera;
 *     vec2 resolution;
 *     uint utime;
 * } global;
 *
 *
 */
public class SceneDescriptorSet extends VulkanObject {

    private final static FrameKey<VkBuffer>        FK_BUFFER = FrameKey.of( VkBuffer.class );
    private final static FrameKey<VkDescriptorSet> FK_DESCRIPTORSET = FrameKey.of( VkDescriptorSet.class );

    private final static int BUFFER_SIZE =
            MemorySizeUtils.MAT4x4F + MemorySizeUtils.MAT4x4F + MemorySizeUtils.MAT4x4F + MemorySizeUtils.MAT4x4F //Projection + View + Project Inv. + View Inv
            + MemorySizeUtils.MAT4x4F //Camera position
            + MemorySizeUtils.VEC2F + MemorySizeUtils.INT; //resolution + utime
    private final static VkBufferDescriptor BUFFER_DESCRIPTOR = new VkBufferDescriptor( BUFFER_SIZE, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0 );

    private final Renderer renderer;

    private VkDescriptorSetLayout descriptorSetLayout;
    private VkDescriptorPool descriptorPool;

    private final Matrix4f wInvProjection = new Matrix4f();
    private final Matrix4f wInvView = new Matrix4f();
    private Long utime = null;

    public SceneDescriptorSet(Configuration configuration, Renderer renderer ) {
        super( configuration );
        this.renderer = renderer;
    }

    public void updateAll( Scene scene ) throws ThemisException {
        this.renderer.getFrames().update( FK_BUFFER, (frame, buffer) -> update( frame, scene ) );
    }

    public void update( int frame, Scene scene ) {

        if ( this.utime == null ) {
            this.utime = System.currentTimeMillis();
        }

        this.wInvProjection.set( scene.getProjection().perspective() ).invert();
        this.wInvView.set( scene.getCamera().matrix() ).invert();

        VkBuffer buffer = this.renderer.getFrames().get( frame, FK_BUFFER );
        buffer.set( 0, scene.getProjection().perspective() );
        buffer.set( MemorySizeUtils.MAT4x4F, scene.getCamera().matrix() );
        buffer.set( MemorySizeUtils.MAT4x4F * 2, this.wInvProjection );
        buffer.set( MemorySizeUtils.MAT4x4F * 3, this.wInvView );
        buffer.set( MemorySizeUtils.MAT4x4F * 4, scene.getCamera().getPosition() );
        buffer.set( MemorySizeUtils.MAT4x4F * 4 + MemorySizeUtils.VEC4F, this.renderer.getWindow().getResolution() );
        buffer.set( MemorySizeUtils.MAT4x4F * 4 + MemorySizeUtils.VEC4F + MemorySizeUtils.VEC2I, (int) (System.currentTimeMillis() - this.utime) );

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
            VkDescriptorSetBinding.uniform(0, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT)
        );
        this.descriptorSetLayout.setup();
    }

    private void setupDescriptorPool() throws ThemisException {
        this.descriptorPool = new VkDescriptorPool(getConfiguration(), this.renderer.getDevice(), this.renderer.getFrames().getSize(), this.descriptorSetLayout );
        this.descriptorPool.setup();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.renderer.getFrames().remove( FK_BUFFER );
        this.renderer.getFrames().remove( FK_DESCRIPTORSET );
        this.descriptorPool.cleanup();
        this.descriptorSetLayout.cleanup();
    }
}
