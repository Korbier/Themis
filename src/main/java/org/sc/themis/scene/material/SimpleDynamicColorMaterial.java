package org.sc.themis.scene.material;

import org.joml.Vector4f;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.scene.Mesh;
import org.sc.themis.scene.MeshProperties;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

/**
 * Color material.
 * Apply to the mesh the color defined on MaterialAttribute.Color.BASE attribute
 *
 * Shader source :
 *
 * layout(std140, set = X, binding = 0) uniform Material {
 *     vec4 color;
 * } material;
 *
 */
public class SimpleDynamicColorMaterial extends Material {

    public final static String MATERIAL_ID = "Material.SimpleDynamicColorMaterial";

    private final FrameKey<VkBuffer> fkBuffersA = FrameKey.of( VkBuffer.class );
    private final FrameKey<VkBuffer> fkBuffersB = FrameKey.of( VkBuffer.class );

    private final Map<String, Integer> materialIndexA = new HashMap<>();
    private final Map<String, Integer> materialIndexB = new HashMap<>();

    public SimpleDynamicColorMaterial(Configuration configuration, Renderer renderer ) {
        super( configuration, renderer, MATERIAL_ID );
        setDescriptorsetIdentifier( mesh -> mesh.getProperty(MeshProperties.COLOR_BASE).toString() );
    }

    protected VkDescriptorSetBinding [] getMainDescriptorSetBindings() {
        return new VkDescriptorSetBinding[] {
            VkDescriptorSetBinding.dynamicUniform( 0, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT),
            VkDescriptorSetBinding.dynamicUniform( 1, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT),
        };
    }

    @Override
    protected void setupMainMaterialLayout( FrameKey<VkDescriptorSet> descriptorSetKey, Mesh... meshes ) throws ThemisException {

        //Get distinct values
        Set<String> values = new HashSet<>();
        for ( Mesh mesh : meshes) {
            values.add( getDescriptorsetIdentifier( mesh ) );
        }

        VkBufferDescriptor bufferDescriptor = VkBufferDescriptor.descriptorsetDynamicUniform( MemorySizeUtils.VEC4F, values.size() );

        //Creation du back buffer du descriptorset A
        getFrames().create( this.fkBuffersA, () -> new VkBuffer(getConfiguration(), getDevice(), getAllocator(), bufferDescriptor) );
        getFrames().update( this.fkBuffersA, (buffer) -> {
            int offset = 0;
            for ( Mesh mesh : meshes ) {
                String identifier = getDescriptorsetIdentifier( mesh );
                if ( !this.materialIndexA.containsKey( identifier ) ) {
                    int off = buffer.getAlignedOffset( offset++ );
                    this.materialIndexA.put(identifier, off);
                    buffer.set(off, mesh.getProperty(MeshProperties.COLOR_BASE));
                }else {
                    buffer.set(this.materialIndexA.get(identifier), mesh.getProperty(MeshProperties.COLOR_BASE));
                }
            }
        } );

        //Creation du back buffer du descriptorset B
        getFrames().create( this.fkBuffersB, () -> new VkBuffer(getConfiguration(), getDevice(), getAllocator(), bufferDescriptor) );
        getFrames().update( this.fkBuffersB, (buffer) -> {
            int offset = 0;
            for ( Mesh mesh : meshes ) {
                String identifier = getDescriptorsetIdentifier( mesh );
                if ( !this.materialIndexB.containsKey( identifier ) ) {
                    int off = buffer.getAlignedOffset(offset++);
                    this.materialIndexB.put(identifier, off);
                    buffer.set(off, new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
                } else {
                    buffer.set(this.materialIndexB.get(identifier), new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
                }
            }
        } );

        //Bind du buffer au descriptorset
        getFrames().update(descriptorSetKey, (frame, descriptorset) -> {
            descriptorset.bind(0, getFrames().get(frame, this.fkBuffersA) );
            descriptorset.bind(1, getFrames().get(frame, this.fkBuffersB) );
        } );

        System.out.println( "Descriptorsets : " + this.materialIndexA.size() );
    }

    protected int getBackBufferDynamicOffset(String material, int frame, int binding) {
        return binding == 0 ? this.materialIndexA.get( material ) : this.materialIndexB.get( material ) ;
    }


    @Override
    protected void cleanupMaterialLayout() throws ThemisException {
        getFrames().remove( this.fkBuffersA );
        getFrames().remove( this.fkBuffersB );
    }

}
