package org.sc.themis.scene.material;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.device.VkMemoryAllocator;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.scene.Mesh;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.*;
import java.util.function.Function;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;

public abstract class Material extends VulkanObject {

    private final Renderer renderer;
    private final String identifier;

    private VkDescriptorSetLayout mainDescriptorSetLayout;
    private final FrameKey<VkDescriptorSet> fkMainDescriptorSet = FrameKey.of( VkDescriptorSet.class );
    private VkDescriptorPool mainDescriptorPool;

    private VkDescriptorSetLayout descriptorSetLayout;
    private final Map<String, FrameKey<VkDescriptorSet>> fkDescriptorsets = new HashMap<>();
    private VkDescriptorPool descriptorPool;

    private int [] wDynamicOffset = new int[0];
    private Function<Mesh, String> descriptorSetIdentifierFunction = Mesh::getIdentifier;

    public Material( Configuration configuration, Renderer renderer, String identifier ) {
        super( configuration );
        this.renderer = renderer;
        this.identifier = identifier;
    }

    protected VkDescriptorSetBinding [] getDescriptorSetBindings() { return new VkDescriptorSetBinding [0]; };
    protected void setupMaterialLayout( FrameKey<VkDescriptorSet> descriptorSetKey, Mesh mesh ) throws ThemisException {};
    protected int getBackBufferDynamicOffset(String material, int frame, int binding) { return -1; }
    protected void cleanupMaterialLayout() throws ThemisException {};

    protected VkDescriptorSetBinding [] getMainDescriptorSetBindings() { return new VkDescriptorSetBinding [0]; };
    protected void setupMainMaterialLayout( FrameKey<VkDescriptorSet> descriptorSetKey, Mesh ... meshes ) throws ThemisException {};
    protected void cleanupMainMaterialLayout() throws ThemisException {};

    protected String getDescriptorsetIdentifier(Mesh mesh ) {
        return this.descriptorSetIdentifierFunction.apply( mesh );
    }

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

    public void setDescriptorsetIdentifier( Function<Mesh, String> function ) {
        this.descriptorSetIdentifierFunction = function;
    }

    @Override
    public void setup() throws ThemisException {
        setupMainDescriptorLayout();
        setupDescriptorLayout();
    }

    @Override
    public void cleanup() throws ThemisException {

        this.cleanupMaterialLayout();
        this.cleanupMainMaterialLayout();

        if ( this.descriptorPool != null ) this.descriptorPool.cleanup();
        if ( this.descriptorSetLayout != null ) this.descriptorSetLayout.cleanup();
        if ( this.mainDescriptorPool != null ) this.mainDescriptorPool.cleanup();
        if ( this.mainDescriptorSetLayout != null ) this.mainDescriptorSetLayout.cleanup();

    }

    public void setupScene( Scene scene ) throws ThemisException {

        List<Mesh> meshes = new ArrayList<>();

        for ( Model model : scene.getModels() ) {
            for (Mesh mesh : model.getMeshes() ) {
                if ( getIdentifier().equals( mesh.getMaterial() ) ) {
                    meshes.add( mesh );
                }
            }
        }

        setupDescriptorPool( meshes );
        setupDescriptorSets( meshes );
    }

    public VkDescriptorSetLayout [] getDescriptorSetLayout() {

        int count = 0;
        if ( this.mainDescriptorSetLayout != null ) count++;
        if ( this.descriptorSetLayout != null ) count++;

        VkDescriptorSetLayout [] layouts = new VkDescriptorSetLayout[count];
        if ( this.descriptorSetLayout != null ) layouts[--count] = this.descriptorSetLayout;
        if ( this.mainDescriptorSetLayout != null ) layouts[--count] = this.mainDescriptorSetLayout;

        return layouts;

    }

    public VkDescriptorSet [] getDescriptorSet( Mesh mesh, int frame ) {

        int count = 0;
        if ( this.mainDescriptorSetLayout != null ) count++;
        if ( this.descriptorSetLayout != null ) count++;

        VkDescriptorSet [] descriptorsets = new VkDescriptorSet[count];
        if ( this.descriptorSetLayout != null ) descriptorsets[--count] = getFrames().get( frame, this.fkDescriptorsets.get( getDescriptorsetIdentifier( mesh ) ) );
        if ( this.mainDescriptorSetLayout != null ) descriptorsets[--count] = getFrames().get( frame, this.fkMainDescriptorSet );

        return descriptorsets;

    }

    public int [] getDynamicOffset( Mesh mesh, int frame ) {

        for ( int i=0; i<this.wDynamicOffset.length; i++ ) {
            this.wDynamicOffset[i] = getBackBufferDynamicOffset( getDescriptorsetIdentifier( mesh ), frame, i );
        }

        return this.wDynamicOffset;

    }

    private void setupMainDescriptorLayout() throws ThemisException {

        VkDescriptorSetBinding [] bindings = getMainDescriptorSetBindings();

        if ( bindings.length > 0 ) {

            this.mainDescriptorSetLayout = new VkDescriptorSetLayout( getConfiguration(), getDevice(),bindings );
            this.mainDescriptorSetLayout.setup();

            int countDynamics = 0;

            for ( VkDescriptorSetBinding b : bindings ) {
                if ( b.getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC ) countDynamics++;
            }

            this.wDynamicOffset = new int[countDynamics];

        }

    }

    private void setupDescriptorLayout() throws ThemisException {

        VkDescriptorSetBinding [] bindings = getDescriptorSetBindings();

        if ( bindings.length > 0 ) {
            this.descriptorSetLayout = new VkDescriptorSetLayout(getConfiguration(), getDevice(), bindings);
            this.descriptorSetLayout.setup();
        }

    }

    private void setupDescriptorPool( List<Mesh> meshes ) throws ThemisException {

        if ( this.descriptorSetLayout != null ) {
            this.descriptorPool = new VkDescriptorPool(getConfiguration(), getDevice(), getFrames().getSize() * meshes.size(), this.descriptorSetLayout);
            this.descriptorPool.setup();
        }

        if ( this.mainDescriptorSetLayout != null ) {
            this.mainDescriptorPool = new VkDescriptorPool(getConfiguration(), getDevice(), getFrames().getSize(), this.mainDescriptorSetLayout);
            this.mainDescriptorPool.setup();
        }

    }

    private void setupDescriptorSets( List<Mesh> meshes ) throws ThemisException {

        if ( this.mainDescriptorSetLayout != null ) {
            getFrames().create(this.fkMainDescriptorSet, () -> new VkDescriptorSet(getConfiguration(), getDevice(), this.mainDescriptorPool, this.mainDescriptorSetLayout));
            setupMainMaterialLayout(this.fkMainDescriptorSet, meshes.toArray(new Mesh[0]));
        }

        if ( this.descriptorSetLayout != null ) {

            for (Mesh mesh : meshes) {
                FrameKey<VkDescriptorSet> key = FrameKey.of(VkDescriptorSet.class);
                this.fkDescriptorsets.put( getDescriptorsetIdentifier( mesh ), key);
                getFrames().create(key, () -> new VkDescriptorSet(getConfiguration(), getDevice(), this.descriptorPool, this.descriptorSetLayout));
                setupMaterialLayout(key, mesh );
            }

        }

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

}
