package org.sc.themis.scene.material;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.device.VkMemoryAllocator;
import org.sc.themis.renderer.pipeline.*;
import org.sc.themis.renderer.pipeline.descriptorset.*;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.resource.image.VkSampler;
import org.sc.themis.renderer.resource.image.VkSamplerDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.*;
import java.util.function.Function;

public abstract class Material extends VulkanObject {

    private static final int DESCRIPTORPOOL_SIZE = 10;

    @FunctionalInterface
    public interface UniformSetter {
        void set(int binding, VkBuffer buffer, MaterialProperties properties );
    }

    @FunctionalInterface
    public interface CombinedImageSamplerSetter {
        void set(int binding, VkDescriptorSet descriptorset, VkSampler sampler, MaterialProperties meshProperties );
    }

    private final Renderer renderer;
    private final String identifier;

    /** Variant identifier function **/
    private Function<MaterialProperties,String> variantIdentifierFunction = MaterialProperties::toString;

    /** Pipeline **/
    private final MaterialPipeline pipeline;

    /** Variants **/
    private final Map<String, MaterialVariant> variants = new HashMap<>();
    private final MaterialVariantDescriptor variantsDescriptor = new MaterialVariantDescriptor();
    private VkDescriptorSetLayout variantsDescriptorSetLayout;
    private VkDescriptorPool variantsDescriptorPool;
    private final List<VkDescriptorPool> oldVariantsDescriptorPools = new ArrayList<>();
    private UniformSetter variantsUniformSetter;
    private CombinedImageSamplerSetter variantsCombinedImageSamplerSetter;

    /** Others Descriptorset and descriptorsetLayout **/
    private VkDescriptorSetProvider [] descriptorsetProviders;

    public Material( Configuration configuration, Renderer renderer, String identifier ) {
        super( configuration );
        this.renderer = renderer;
        this.identifier = identifier;
        this.pipeline = new MaterialPipeline( configuration, renderer, this );
    }

    @Override
    public void setup() throws ThemisException {
        this.setupVariantsDescriptorsetLayout();
        this.setupVariantsDescriptorPool();
        this.pipeline.setup( collectDescriptorsetLayouts() );
    }

    @Override
    public void cleanup() throws ThemisException {
        this.pipeline.cleanup();
        for ( VkDescriptorPool pool : this.oldVariantsDescriptorPools ) pool.cleanup();
        for ( MaterialVariant variant : this.variants.values() ) variant.cleanup();
        this.variantsDescriptorPool.cleanup();
        this.variantsDescriptorSetLayout.cleanup();
    }

    /** Material usage methods - create and store variant for provided properties **/
    public String add( MaterialProperties properties ) throws ThemisException {

        String variantIdentifier = getVariantIdentifier( properties );

        if ( this.variants.containsKey( variantIdentifier ) ) {
            return variantIdentifier;
        }

        MaterialVariant variant = new MaterialVariant( getConfiguration(), this, variantIdentifier );
        variant.setup();
        variant.setProperties( properties );

        this.variants.put( variantIdentifier, variant );

        return variantIdentifier;

    }

    /** Material building methods - Variant Identifier function **/
    protected void setVariantsIdentifierFunction( Function<MaterialProperties,String> variantIdentifierFunction ) {
        this.variantIdentifierFunction = variantIdentifierFunction;
    }

    /** Material building methods - Pipeline **/
    public void addShader( int shaderStage, byte [] source ) {
        this.pipeline.addShader( shaderStage, source );
    }

    public void addConstantRange( int stage, int offset, int size ) {
        this.pipeline.addConstantRange( stage, offset, size );
    }

    public void setVertexInputDescriptor( VkVertexInputStateDescriptor descriptor ) {
        this.pipeline.setVertexInputDescriptor( descriptor );
    }

    public void setPipelineDescriptor( VkPipelineDescriptor descriptor) {
        this.pipeline.setPipelineDescriptor( descriptor );
    }

    /** Material building methods - Variant **/
    protected void addVariantsUniformBinding( int binding, int shaderStage, VkBufferDescriptor bufferDescriptor ) {
        this.variantsDescriptor.addUniformBinding( binding, shaderStage, bufferDescriptor );
    }

    protected void addVariantsCombinedImageSamplerBinding( int binding, int shaderStage, VkSamplerDescriptor samplerDescriptor ) {
        this.variantsDescriptor.addCombinedImageSamplerBinding( binding, shaderStage, samplerDescriptor );
    }

    protected void setVariantsUniformSetter( UniformSetter uniformSetter ) {
        this.variantsUniformSetter = uniformSetter;
    }

    protected void setVariantsCombinedImageSamplerSetter( CombinedImageSamplerSetter combinedImageSamplerSetter ) {
        this.variantsCombinedImageSamplerSetter = combinedImageSamplerSetter;
    }

    /** Others Descriptorsets and descriptorsetLayouts **/
    public void setDescriptorsetProviders( VkDescriptorSetProvider ... providers ) {
        this.descriptorsetProviders = providers;
    }

    /** Getters **/

    public String getIdentifier() {
        return this.identifier;
    }

    protected VkDevice getDevice() {
        return this.renderer.getDevice();
    }

    protected Frames getFrames() {
        return this.renderer.getFrames();
    }

    protected VkMemoryAllocator getAllocator() {
        return this.renderer.getMemoryAllocator();
    }

    public VkPipeline getPipeline() {
        return this.pipeline.getPipeline();
    }

    public UniformSetter getVariantsUniformSetter() {
        return this.variantsUniformSetter;
    }

    public CombinedImageSamplerSetter getVariantsCombinedImageSamplerSetter() {
        return this.variantsCombinedImageSamplerSetter;
    }

    public VkDescriptorPool getVariantsDescriptorPool() throws ThemisException {

        if ( this.variantsDescriptorPool.isFull() ) {
            this.oldVariantsDescriptorPools.add( this.variantsDescriptorPool );
            setupVariantsDescriptorPool();
        }

        return this.variantsDescriptorPool;

    }

    public MaterialVariantDescriptor getVariantsDescriptor() {
        return this.variantsDescriptor;
    }

    public VkDescriptorSet [] getDescriptorSets( int frame, MaterialProperties properties ) {

        String variantIdentifier = getVariantIdentifier( properties );

        int count = this.descriptorsetProviders.length;
        if ( this.variantsDescriptorSetLayout != null ) count++;

        VkDescriptorSet [] descriptorsets = new VkDescriptorSet[count];
        if ( this.variantsDescriptorSetLayout != null ) descriptorsets[--count] = this.variants.get( variantIdentifier ).getDescriptorSet( frame );
        //if ( this.mainDescriptorSetLayout != null ) descriptorsets[--count] = getFrames().get( frame, this.fkMainDescriptorSet );
        for ( int i = count - 1; i >= 0; i-- ) descriptorsets[i] = this.descriptorsetProviders[i].getDescriptorSet( frame );

        return descriptorsets;

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material mesh = (Material) o;
        return Objects.equals(identifier, mesh.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }

    private String getVariantIdentifier( MaterialProperties properties ) {
        return this.variantIdentifierFunction.apply(properties);
    }

    private void setupVariantsDescriptorsetLayout() throws ThemisException {
        VkDescriptorSetBinding[] bindings = this.variantsDescriptor.getBindings().values().toArray(new VkDescriptorSetBinding[0]);
        if ( bindings.length > 0 ) {
            this.variantsDescriptorSetLayout = new VkDescriptorSetLayout(getConfiguration(), getDevice(), bindings);
            this.variantsDescriptorSetLayout.setup();
        }
    }

    private void setupVariantsDescriptorPool() throws ThemisException {
        if ( this.variantsDescriptorSetLayout != null ) {
            this.variantsDescriptorPool = new VkDescriptorPool(getConfiguration(), getDevice(), getFrames().getSize() * Material.DESCRIPTORPOOL_SIZE, this.variantsDescriptorSetLayout );
            this.variantsDescriptorPool.setup();
        }
    }

    private VkDescriptorSetLayout [] collectDescriptorsetLayouts() {

        int count = this.descriptorsetProviders != null ? this.descriptorsetProviders.length : 0;
        if ( this.variantsDescriptorSetLayout != null ) count++;

        VkDescriptorSetLayout [] layouts = new VkDescriptorSetLayout[count];
        if ( this.variantsDescriptorSetLayout != null ) layouts[--count] = this.variantsDescriptorSetLayout;
        if ( this.descriptorsetProviders != null )  {
            for ( int i = count - 1; i >= 0; i-- ) layouts[i] = this.descriptorsetProviders[i].getDescriptorSetLayout();
        }

        return layouts;

    }

}
