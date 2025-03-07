package org.sc.themis.renderer.material;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;

import java.util.HashMap;
import java.util.Map;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

public class MaterialMainVariant extends TObject {


    private final String identifier;
    private final Material material;

    private final FrameKey<VkDescriptorSet> descriptorset = FrameKey.of( VkDescriptorSet.class );
    private final Map<Integer, FrameKey<VkBuffer>>  buffers = new HashMap<>();

    public MaterialMainVariant(Configuration configuration, Material material, String identifier ) {
        super( configuration );
        this.material = material;
        this.identifier = identifier;
    }

    public Material getMaterial() {
        return this.material;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setup() throws ThemisException {
        setupDescriptorset();
        setupUniformDynamic();
    }

    public VkDescriptorSet getDescriptorSet( int frame ) {
        return this.material.getFrames().get( frame, this.descriptorset );
    }

    public int getAlignedOffset( int frame, int binding, int requestOffset ) {
        VkBuffer buffer = this.material.getFrames().get( frame, this.buffers.get( binding ) );
        return buffer.isAligned() ? requestOffset * buffer.getAlignedSize() : requestOffset;
    }

    public void setProperties( int offset, MaterialProperties properties ) throws ThemisException {

        for (Map.Entry<Integer, VkDescriptorSetBinding> bindingEntry : this.material.getMainDescriptor().getBindings().entrySet()) {

            int bindingIdx = bindingEntry.getKey();
            VkDescriptorSetBinding binding = bindingEntry.getValue();

            if ( binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC ) {
                FrameKey<VkBuffer> bufferKey = this.buffers.get( bindingIdx );
                this.material.getFrames().update( bufferKey, (buffer) -> this.material.getMainUniformSetter().set( bindingIdx, buffer, offset, properties ));
            }

        }

    }

    public void cleanup() throws ThemisException {
        this.material.getFrames().remove( this.descriptorset );
    }

    private void setupDescriptorset() throws ThemisException {
        VkDescriptorPool pool = this.material.getMainDescriptorPool();
        this.material.getFrames().create( this.descriptorset, pool::create );
    }

    private void setupUniformDynamic() throws ThemisException {

        for ( Map.Entry<Integer, VkDescriptorSetBinding> bindingEntry : this.material.getVariantsDescriptor().getBindings().entrySet() ) {

            int bindingIdx = bindingEntry.getKey();
            VkDescriptorSetBinding binding = bindingEntry.getValue();
            VkBufferDescriptor     bufferDescriptor = this.material.getVariantsDescriptor().getBufferDescriptor( bindingIdx );

            if ( binding.getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER && bufferDescriptor != null ) {
                FrameKey<VkBuffer> bufferKey = FrameKey.of(VkBuffer.class);
                this.buffers.put( bindingIdx, bufferKey);
                this.material.getFrames().create( bufferKey, () -> new VkBuffer(getConfiguration(), this.material.getDevice(), this.material.getAllocator(), bufferDescriptor) );
                this.material.getFrames().update( this.descriptorset, (frame, descriptorset) -> descriptorset.bind(bindingIdx, this.material.getFrames().get(frame, bufferKey) ) );
            }

        }

    }

}
