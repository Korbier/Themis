package org.sc.themis.scene.material;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.pipeline.descriptorset.*;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.resource.image.VkSampler;
import org.sc.themis.renderer.resource.image.VkSamplerDescriptor;
import org.sc.themis.scene.Mesh;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.exception.MaterialException;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

public abstract class BaseMaterial extends Material {

    private final VkDescriptorSetProvider [] descriptorSetProviders;

    private Function<Mesh,String> variantIdentifierFunction = Mesh::getIdentifier;

    /** Variant inputs **/
    private final Map<Integer, VkDescriptorSetBinding> variantBindings = new HashMap<>();
    private final Map<Integer, VkBufferDescriptor> variantBufferDescriptors = new HashMap<>();
    private final Map<Integer, VkSamplerDescriptor> variantSamplerDescriptors = new HashMap<>();

    /** Variant descriptorsets data **/
    private VkDescriptorSetLayout variantDescriptorSetLayout;
    private VkDescriptorPool variantDescriptorPool;
    private final Map<String, FrameKey<VkDescriptorSet>> variantDescriptorsets = new HashMap<>();

    /** Variant backends **/
    private final Map<String, VariantBuffers> variantBuffers = new HashMap<>();
    private final Map<String, VariantSamplers> variantSamplers = new HashMap<>();

    public BaseMaterial(Configuration configuration, Renderer renderer, String identifier, VkDescriptorSetProvider... descriptorSetProviders) {
        super(configuration, renderer, identifier);
        this.descriptorSetProviders = descriptorSetProviders;
    }

    @Override
    public void setup() throws ThemisException {
        setupVariantDescriptorLayout();
        super.setup();
    }

    @Override
    public void setup(Scene scene) throws ThemisException {

        List<Mesh> meshes = new ArrayList<>();

        for ( Model model : scene.getModels() ) {
            for (Mesh mesh : model.getMeshes() ) {
                if ( getIdentifier().equals( mesh.getMaterialIdentifier() ) ) {
                    meshes.add( mesh );
                }
            }
        }

        if ( !meshes.isEmpty() ) {
            setupVariantDescriptorPool(meshes);
            setupVariantDescriptorset(meshes);
        }

    }

    @Override
    public void cleanup() throws ThemisException {

        super.cleanup();

        for ( VariantBuffers variantBuffers : this.variantBuffers.values() ) {
            variantBuffers.cleanup( getFrames() );
        }

        for ( FrameKey<VkDescriptorSet> descKey : this.variantDescriptorsets.values() ) {
            getFrames().remove( descKey );
        }

        if ( this.variantDescriptorPool != null ) this.variantDescriptorPool.cleanup();
        if ( this.variantDescriptorSetLayout != null ) this.variantDescriptorSetLayout.cleanup();

    }

    public abstract void set( int binding, VkBuffer buffer, Mesh mesh );
    public abstract void set( int binding, VkDescriptorSet descriptorset, VkSampler sampler, Mesh mesh );

    public String getVariantIdentifier( Mesh mesh ) {
        return this.variantIdentifierFunction.apply( mesh );
    }

    public VkDescriptorSetLayout [] getDescriptorSetLayout() {

        int count = this.descriptorSetProviders.length;
        //if ( this.mainDescriptorSetLayout != null ) count++;
        if ( this.variantDescriptorSetLayout != null ) count++;

        VkDescriptorSetLayout [] layouts = new VkDescriptorSetLayout[count];
        if ( this.variantDescriptorSetLayout != null ) layouts[--count] = this.variantDescriptorSetLayout;
        //if ( this.mainDescriptorSetLayout != null ) layouts[--count] = this.mainDescriptorSetLayout;
        for ( int i = count - 1; i >= 0; i-- ) layouts[i] = this.descriptorSetProviders[i].getDescriptorSetLayout();

        return layouts;

    }

    public VkDescriptorSet [] getDescriptorSet( Mesh mesh, int frame ) {

        int count = this.descriptorSetProviders.length;
        //if ( this.mainDescriptorSetLayout != null ) count++;
        if ( this.variantDescriptorSetLayout != null ) count++;

        VkDescriptorSet [] descriptorsets = new VkDescriptorSet[count];
        if ( this.variantDescriptorSetLayout != null ) descriptorsets[--count] = getFrames().get( frame, this.variantDescriptorsets.get( getVariantIdentifier( mesh ) ) );
        //if ( this.mainDescriptorSetLayout != null ) descriptorsets[--count] = getFrames().get( frame, this.fkMainDescriptorSet );
        for ( int i = count - 1; i >= 0; i-- ) descriptorsets[i] = this.descriptorSetProviders[i].getDescriptorSet( frame );

        return descriptorsets;

    }

    protected void setVariantIdentifierFunction( Function<Mesh,String> variantIdentifierFunction ) {
        this.variantIdentifierFunction = variantIdentifierFunction;
    }

    protected void addVariantUniformBinding( int binding, int shaderStage, VkBufferDescriptor bufferDescriptor ) {
        this.variantBindings.put( binding, VkDescriptorSetBinding.uniform( binding, shaderStage) );
        this.variantBufferDescriptors.put( binding, bufferDescriptor );
    }

    protected void addVariantImageSamplerBinding( int binding, int shaderStage, VkSamplerDescriptor samplerDescriptor ) {
        this.variantBindings.put( binding, VkDescriptorSetBinding.combinedImageSampler( binding, shaderStage) );
        this.variantSamplerDescriptors.put( binding, samplerDescriptor );
    }

    private void setupVariantDescriptorLayout() throws ThemisException {
        VkDescriptorSetBinding [] bindings = this.variantBindings.values().toArray(new VkDescriptorSetBinding[0]);
        if ( bindings.length > 0 ) {
            this.variantDescriptorSetLayout = new VkDescriptorSetLayout(getConfiguration(), getDevice(), bindings);
            this.variantDescriptorSetLayout.setup();
        }
    }

    private void setupVariantDescriptorPool( List<Mesh> meshes ) throws ThemisException {
        if ( this.variantDescriptorSetLayout != null ) {
            this.variantDescriptorPool = new VkDescriptorPool(getConfiguration(), getDevice(), getFrames().getSize() * meshes.size(), this.variantDescriptorSetLayout );
            this.variantDescriptorPool.setup();
        }
    }

    private void setupVariantDescriptorset(List<Mesh> meshes) throws ThemisException {

        for ( Mesh mesh : meshes ) {

            String variantIdentifier = getVariantIdentifier( mesh );

            if ( !this.variantDescriptorsets.containsKey( variantIdentifier ) ) {
                FrameKey<VkDescriptorSet> key = createVariantDescriptorset( variantIdentifier );
                for ( VkDescriptorSetBinding binding : this.variantBindings.values() ) {
                    switch( binding.getDescriptorType() ) {
                        case VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER -> setupVariantUniform( variantIdentifier, key, binding.getBinding(), mesh );
                        case VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER -> setupVariantCombinedImageSampler( variantIdentifier, key, binding.getBinding(), mesh );
                    }
                }
            }

        }

    }

    /** UNIFORM **/

    private void setupVariantUniform(String variantIdentifier, FrameKey<VkDescriptorSet> key, int binding, Mesh mesh ) throws ThemisException {

        Assertions.notNull( this.variantBufferDescriptors.get( binding ), new MaterialException("No buffer descriptor set for binding " + binding + " of type VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER" ) );

        createVariantBindingBuffer(variantIdentifier, key, binding, this.variantBufferDescriptors.get( binding ) );
        variantBufferSet( variantIdentifier, binding, mesh );

    }

    private void createVariantBindingBuffer(String variantIdentifier, FrameKey<VkDescriptorSet> key, int binding, VkBufferDescriptor vkBufferDescriptor) throws ThemisException {

        VariantBuffers buffers = this.variantBuffers.get( variantIdentifier );
        if ( buffers == null ) {
            buffers = new VariantBuffers();
            this.variantBuffers.put( variantIdentifier, buffers );
        }

        FrameKey<VkBuffer> bufferKey = buffers.add( binding );

        //Creation du back buffer du descriptorset
        getFrames().create( bufferKey, () -> new VkBuffer(getConfiguration(), getDevice(), getAllocator(), vkBufferDescriptor) );
        //Bind du buffer au descriptorset
        getFrames().update( key, (frame, descriptorset) -> descriptorset.bind(binding, getFrames().get(frame, bufferKey) ) );

    }

    private void variantBufferSet( String variantIdentifier, int binding, Mesh mesh ) throws ThemisException {

        VariantBuffers     buffers   = this.variantBuffers.get( variantIdentifier );
        FrameKey<VkBuffer> bufferKey = buffers.getBufferKey( binding );

        getFrames().update( bufferKey, (buffer) -> this.set(0, buffer, mesh ));

    }

    /** COMBINED IMAGE SAMPLER **/

    private void setupVariantCombinedImageSampler(String variantIdentifier, FrameKey<VkDescriptorSet> key, int binding, Mesh mesh) throws ThemisException {

        Assertions.notNull( this.variantSamplerDescriptors.get( binding ), new MaterialException("No sampler descriptor set for binding " + binding + " of type VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER" ) );

        createVariantBindingSampler(variantIdentifier, key, binding, this.variantSamplerDescriptors.get( binding ) );
        variantImageSet( variantIdentifier, key, binding, mesh );

    }

    private void createVariantBindingSampler(String variantIdentifier, FrameKey<VkDescriptorSet> key, int binding, VkSamplerDescriptor vkSamplerDescriptor) throws ThemisException {


        VariantSamplers samplers = this.variantSamplers.get( variantIdentifier );
        if ( samplers == null ) {
            samplers = new VariantSamplers();
            this.variantSamplers.put( variantIdentifier, samplers );
        }

        FrameKey<VkSampler> samplerKey = samplers.add( binding );

        //Creation du back buffer du descriptorset
        getFrames().create( samplerKey, () -> new VkSampler( getConfiguration(), getDevice(), vkSamplerDescriptor) );


    }

    private void variantImageSet( String variantIdentifier, FrameKey<VkDescriptorSet> key, int binding, Mesh mesh ) throws ThemisException {

        VariantSamplers     samplers   = this.variantSamplers.get( variantIdentifier );
        FrameKey<VkSampler> samplerKey = samplers.getSamplerKey( binding );

        //Bind du buffer au descriptorset
        getFrames().update( key, (frame, descriptorset) -> this.set( binding, descriptorset, getFrames().get(frame, samplerKey), mesh ) );

    }


    private FrameKey<VkDescriptorSet> createVariantDescriptorset( String variantIdentifier ) throws ThemisException {
        FrameKey<VkDescriptorSet> key = FrameKey.of(VkDescriptorSet.class);
        this.variantDescriptorsets.put( variantIdentifier, key );
        getFrames().create( key, () -> new VkDescriptorSet( getConfiguration(), getDevice(), this.variantDescriptorPool, this.variantDescriptorSetLayout ) );
        return key;
    }

    private static class VariantBuffers {

        private final Map<Integer, FrameKey<VkBuffer>> keys = new HashMap<>();

        public FrameKey<VkBuffer> add( int binding ) {
            FrameKey<VkBuffer> bufferKey = FrameKey.of(VkBuffer.class);
            this.keys.put( binding, bufferKey );
            return bufferKey;
        }

        public FrameKey<VkBuffer> getBufferKey( int binding ) {
            return this.keys.get( binding );
        }

        public void cleanup( Frames frames ) throws ThemisException {
            for ( FrameKey<VkBuffer> bufferKey : keys.values() ) {
                frames.remove( bufferKey );
            }
        }

    }

    private static class VariantSamplers {

        private final Map<Integer, FrameKey<VkSampler>> keys = new HashMap<>();

        public FrameKey<VkSampler> add( int binding ) {
            FrameKey<VkSampler> samplerKey = FrameKey.of(VkSampler.class);
            this.keys.put( binding, samplerKey );
            return samplerKey;
        }

        public FrameKey<VkSampler> getSamplerKey( int binding ) {
            return this.keys.get( binding );
        }

        public void cleanup( Frames frames ) throws ThemisException {
            for ( FrameKey<VkSampler> key : keys.values() ) {
                frames.remove( key );
            }
        }

    }

}
