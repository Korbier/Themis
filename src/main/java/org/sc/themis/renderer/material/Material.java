package org.sc.themis.renderer.material;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.Renderer;
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
import org.sc.themis.shared.tobject.TObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Material extends TObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(Material.class);

    private static final int DESCRIPTORPOOL_SIZE = 10;

    @FunctionalInterface
    public interface UniformDynamicSetter {
        void set(int binding, VkBuffer buffer, int offset, MaterialProperties properties);
    }

    @FunctionalInterface
    public interface UniformSetter {
        void set(int binding, VkBuffer buffer, MaterialProperties properties);
    }

    @FunctionalInterface
    public interface CombinedImageSamplerSetter {
        void set(int binding, VkDescriptorSet descriptorset, VkSampler sampler, MaterialProperties meshProperties) throws ThemisException;
    }

    private final Renderer renderer;
    private final String identifier;

    /** Variant identifier function **/
    private Function<MaterialProperties,String> variantIdentifierFunction = MaterialProperties::toString;
    private Predicate<MaterialProperties> materialPropertiesPredicate = (m) -> true;

    /** Pipeline **/
    private final MaterialPipeline pipeline;

    /** Main **/
    private final Map<String, Integer> variantOffsets = new HashMap<>();
    private final MaterialDescriptor mainDescriptor = new MaterialDescriptor();
    private VkDescriptorSetLayout mainDescriptorSetLayout;
    private VkDescriptorPool mainDescriptorPool;
    private MaterialMainVariant mainVariant;
    private UniformDynamicSetter mainUniformSetter;

    /** Variants **/
    private final Map<String, MaterialVariant> variants = new HashMap<>();
    private final MaterialDescriptor variantsDescriptor = new MaterialDescriptor();
    private VkDescriptorSetLayout variantsDescriptorSetLayout;
    private VkDescriptorPool variantsDescriptorPool;
    private final List<VkDescriptorPool> oldVariantsDescriptorPools = new ArrayList<>();
    private UniformSetter variantsUniformSetter;
    private CombinedImageSamplerSetter variantsCombinedImageSamplerSetter;

    /** Others Descriptorset and descriptorsetLayout **/
    private VkDescriptorSetProvider [] descriptorsetProviders;

    public Material(Configuration configuration, Renderer renderer, String identifier) {
        super(configuration);
        this.renderer = renderer;
        this.identifier = identifier;
        this.pipeline = new MaterialPipeline(configuration, renderer);
    }

    @Override
    public void setup() throws ThemisException {
        this.setupVariantsDescriptorsetLayout();
        this.setupVariantsDescriptorPool();
        this.setupMainDescriptorsetLayout();
        this.setupMainDescriptorPool();
        this.pipeline.setup(collectDescriptorsetLayouts());
    }

    @Override
    public void cleanup() throws ThemisException {

        for (VkDescriptorPool pool : this.oldVariantsDescriptorPools) pool.cleanup();
        for (MaterialVariant variant : this.variants.values()) variant.cleanup();

        if (this.variantsDescriptorSetLayout != null) {
            this.variantsDescriptorPool.cleanup();
            this.variantsDescriptorSetLayout.cleanup();
        }

        if (this.mainDescriptorSetLayout != null) {
            this.mainDescriptorPool.cleanup();
            this.mainDescriptorSetLayout.cleanup();
        }

        this.pipeline.cleanup();

    }

    /** Material usage methods - create and store variant for provided properties **/
    public String add(MaterialProperties properties) throws ThemisException {

        if (!this.materialPropertiesPredicate.test(properties)) {
            LOG.warnf("Properties not compatible with material %s", this.getIdentifier());
            return null;
        }

        LOG.infof("New variant for material %s", this.getIdentifier());

        String variantIdentifier = getVariantIdentifier(properties);
        properties.setVariantIdentifier(this, variantIdentifier);

        boolean exists = this.variantOffsets.containsKey(variantIdentifier);

        if (!exists) {

            int offset = this.variantOffsets.size();

            if (this.variantsDescriptorSetLayout != null) {

                if (this.variants.containsKey(variantIdentifier)) {
                    return variantIdentifier;
                }

                MaterialVariant variant = new MaterialVariant(getConfiguration(), this, variantIdentifier);
                variant.setup();
                variant.setProperties(properties);

                this.variants.put(variantIdentifier, variant);

            }

            if (this.mainDescriptorSetLayout != null) {

                //Si il existe déjà un variant principal, on le détruit afin de le recréer en ajoutant
                //les nouvelles propriétés
                if (this.mainVariant != null) {
                    this.mainDescriptorPool.cleanup();
                    this.mainVariant.cleanup();
                }

                this.setupMainDescriptorPool();

                this.mainVariant = new MaterialMainVariant(getConfiguration(), this, getIdentifier() + ".main");
                this.mainVariant.setup();
               // this.mainVariant.setProperties(offset, properties);

            }

            this.variantOffsets.put(variantIdentifier, offset);

        }

        return variantIdentifier;

    }

    /** Material building methods - Variant Identifier function **/
    protected void setMaterialPropertiesValidator(Predicate<MaterialProperties> materialPropertiesPredicate) {
        this.materialPropertiesPredicate = materialPropertiesPredicate;
    }
    protected void setVariantsIdentifierFunction(Function<MaterialProperties,String> variantIdentifierFunction) {
        this.variantIdentifierFunction = variantIdentifierFunction;
    }

    /** Material building methods - Pipeline **/
    public void addShader(int shaderStage, byte [] source) {
        this.pipeline.addShader(shaderStage, source);
    }

    public void addConstantRange(int stage, int offset, int size) {
        this.pipeline.addConstantRange(stage, offset, size);
    }

    public void setVertexInputDescriptor(VkVertexInputStateDescriptor descriptor) {
        this.pipeline.setVertexInputDescriptor(descriptor);
    }

    public void setPipelineDescriptor(VkPipelineDescriptor descriptor) {
        this.pipeline.setPipelineDescriptor(descriptor);
    }

    /** Material building methods - Main **/
    protected void addMainUniformDynamicBinding(int binding, int shaderStage, VkBufferDescriptor bufferDescriptor) {
        this.mainDescriptor.addUniformDynamicBinding(binding, shaderStage, bufferDescriptor);
    }

    protected void setMainUniformSetter(UniformDynamicSetter uniformSetter) {
        this.mainUniformSetter = uniformSetter;
    }

    /** Material building methods - Variant **/
    protected void addVariantsUniformBinding(int binding, int shaderStage, VkBufferDescriptor bufferDescriptor) {
        this.variantsDescriptor.addUniformBinding(binding, shaderStage, bufferDescriptor);
    }

    protected void addVariantsCombinedImageSamplerBinding(int binding, int shaderStage, VkSamplerDescriptor samplerDescriptor) {
        this.variantsDescriptor.addCombinedImageSamplerBinding(binding, shaderStage, samplerDescriptor);
    }

    protected void setVariantsUniformSetter(UniformSetter uniformSetter) {
        this.variantsUniformSetter = uniformSetter;
    }

    protected void setVariantsCombinedImageSamplerSetter(CombinedImageSamplerSetter combinedImageSamplerSetter) {
        this.variantsCombinedImageSamplerSetter = combinedImageSamplerSetter;
    }

    /** Others Descriptorsets and descriptorsetLayouts **/
    public void setDescriptorsetProviders(VkDescriptorSetProvider ... providers) {
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

    public UniformDynamicSetter getMainUniformSetter() {
        return this.mainUniformSetter;
    }

    public VkDescriptorPool getMainDescriptorPool() {
        return this.mainDescriptorPool;
    }

    public MaterialDescriptor getMainDescriptor() {
        return this.mainDescriptor;
    }

    public VkDescriptorPool getVariantsDescriptorPool() throws ThemisException {

        if (this.variantsDescriptorPool.isFull()) {
            this.oldVariantsDescriptorPools.add(this.variantsDescriptorPool);
            setupVariantsDescriptorPool();
        }

        return this.variantsDescriptorPool;

    }

    public MaterialDescriptor getVariantsDescriptor() {
        return this.variantsDescriptor;
    }

    public int [] getDynamicOffset(int frame, MaterialProperties properties) {

        String variantIdentifier = properties.getVariantIdentifier(this);

        int [] offsets = new int[this.mainDescriptorSetLayout.size()];

        for (int i = 0; i < this.mainDescriptorSetLayout.size(); i++) {
            offsets[i] = this.mainVariant.getAlignedOffset(frame, i, this.variantOffsets.get(variantIdentifier));
        }

        return offsets;

    }

    public VkDescriptorSet [] getDescriptorSets(int frame, MaterialProperties properties) {

        String variantIdentifier = properties.getVariantIdentifier(this);

        int count = this.descriptorsetProviders.length;
        if (this.mainDescriptorSetLayout != null) count++;
        if (this.variantsDescriptorSetLayout != null) count++;

        VkDescriptorSet [] descriptorsets = new VkDescriptorSet[count];
        if (this.variantsDescriptorSetLayout != null) descriptorsets[--count] = this.variants.get(variantIdentifier).getDescriptorSet(frame);
        if (this.mainDescriptorSetLayout != null) descriptorsets[--count] = this.mainVariant.getDescriptorSet(frame);
        for (int i = count - 1; i >= 0; i--) descriptorsets[i] = this.descriptorsetProviders[i].getDescriptorSet(frame);

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

    private String getVariantIdentifier(MaterialProperties properties) {
        return this.variantIdentifierFunction.apply(properties);
    }

    private void setupMainDescriptorsetLayout() throws ThemisException {
        VkDescriptorSetBinding[] bindings = this.mainDescriptor.getBindings().values().toArray(new VkDescriptorSetBinding[0]);
        if (bindings.length > 0) {
            this.mainDescriptorSetLayout = new VkDescriptorSetLayout(getConfiguration(), getDevice(), bindings);
            this.mainDescriptorSetLayout.setup();
        }
    }

    private void setupMainDescriptorPool() throws ThemisException {
        if (this.mainDescriptorSetLayout != null) {
            this.mainDescriptorPool = new VkDescriptorPool(getConfiguration(), getDevice(), getFrames().getSize(), this.mainDescriptorSetLayout);
            this.mainDescriptorPool.setup();
        }
    }
    private void setupVariantsDescriptorsetLayout() throws ThemisException {
        VkDescriptorSetBinding[] bindings = this.variantsDescriptor.getBindings().values().toArray(new VkDescriptorSetBinding[0]);
        if (bindings.length > 0) {
            this.variantsDescriptorSetLayout = new VkDescriptorSetLayout(getConfiguration(), getDevice(), bindings);
            this.variantsDescriptorSetLayout.setup();
        }
    }

    private void setupVariantsDescriptorPool() throws ThemisException {
        if (this.variantsDescriptorSetLayout != null) {
            this.variantsDescriptorPool = new VkDescriptorPool(getConfiguration(), getDevice(), getFrames().getSize() * Material.DESCRIPTORPOOL_SIZE, this.variantsDescriptorSetLayout);
            this.variantsDescriptorPool.setup();
        }
    }


    private VkDescriptorSetLayout [] collectDescriptorsetLayouts() {

        int count = this.descriptorsetProviders != null ? this.descriptorsetProviders.length : 0;
        if (this.mainDescriptorSetLayout != null) count++;
        if (this.variantsDescriptorSetLayout != null) count++;

        VkDescriptorSetLayout [] layouts = new VkDescriptorSetLayout[count];
        if (this.variantsDescriptorSetLayout != null) layouts[--count] = this.variantsDescriptorSetLayout;
        if (this.mainDescriptorSetLayout != null) layouts[--count] = this.mainDescriptorSetLayout;
        if (this.descriptorsetProviders != null)  {
            for (int i = count - 1; i >= 0; i--) layouts[i] = this.descriptorsetProviders[i].getDescriptorSetLayout();
        }

        return layouts;

    }

}
