package org.sc.playground.scene.cube2;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.scene.Material;
import org.sc.themis.scene.MaterialAttribute;
import org.sc.themis.scene.descriptorset.VkMaterial;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

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
public class SceneCube2ColorMaterial extends VkMaterial {

    private final static int BUFFER_SIZE = MemorySizeUtils.VEC4F;
    private final static VkBufferDescriptor BUFFER_DESCRIPTOR = VkBufferDescriptor.descriptorsetUniform( BUFFER_SIZE );

    private final Map<String, FrameKey<VkBuffer>> fkBuffers = new HashMap<>();

    public SceneCube2ColorMaterial(Configuration configuration, Renderer renderer ) {
        super( configuration, renderer );
    }

    protected VkDescriptorSetBinding [] getDescriptorSetBindings() {
        return new VkDescriptorSetBinding[] {
            VkDescriptorSetBinding.uniform(0, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT)
        };
    }

    @Override
    protected void setupMaterialLayout( FrameKey<VkDescriptorSet> descriptorSetKey, Material material ) throws ThemisException {

        String identifier = material.getIdentifier();

        //Creation d'une clé pour stocker un buffer par frame
        FrameKey<VkBuffer> key = FrameKey.of(VkBuffer.class);
        this.fkBuffers.put(identifier, key);

        //Creation du back buffer du descriptorset
        getFrames().create( key, () -> new VkBuffer(getConfiguration(), getDevice(), getAllocator(), BUFFER_DESCRIPTOR) );
        //Mise a jour du contenu du buffer
        getFrames().update(key, (buffer) -> buffer.set(0, material.getColor(MaterialAttribute.Color.BASE)));
        //Bind du buffer au descriptorset
        getFrames().update(descriptorSetKey, (frame, descriptorset) -> descriptorset.bind(0, getFrames().get(frame, key) ) );

    }

    @Override
    protected void cleanupMaterialLayout() throws ThemisException {
        for ( FrameKey<VkBuffer> key : this.fkBuffers.values() ) getFrames().remove( key );
    }

}
