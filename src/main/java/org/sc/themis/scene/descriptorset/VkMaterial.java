package org.sc.themis.scene.descriptorset;

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
import org.sc.themis.scene.Material;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;

public abstract class VkMaterial extends VulkanObject {

    private final Renderer renderer;

    private VkDescriptorSetLayout mainDescriptorSetLayout;
    private final FrameKey<VkDescriptorSet> fkMainDescriptorSet = FrameKey.of( VkDescriptorSet.class );
    private VkDescriptorPool mainDescriptorPool;

    private VkDescriptorSetLayout descriptorSetLayout;
    private final Map<String, FrameKey<VkDescriptorSet>> fkDescriptorsets = new HashMap<>();
    private VkDescriptorPool descriptorPool;

    private int [] wDynamicOffset = new int[0];

    public VkMaterial( Configuration configuration, Renderer renderer ) {
        super( configuration );
        this.renderer = renderer;
    }

    protected VkDescriptorSetBinding [] getDescriptorSetBindings() { return new VkDescriptorSetBinding [0]; };
    protected void setupMaterialLayout( FrameKey<VkDescriptorSet> descriptorSetKey, Material material ) throws ThemisException {};
    protected int getBackBufferDynamicOffset(String material, int frame, int binding) { return -1; }
    protected void cleanupMaterialLayout() throws ThemisException {};

    protected VkDescriptorSetBinding [] getMainDescriptorSetBindings() { return new VkDescriptorSetBinding [0]; };
    protected void setupMainMaterialLayout( FrameKey<VkDescriptorSet> descriptorSetKey, Material ...  materials ) throws ThemisException {};
    protected void cleanupMainMaterialLayout() throws ThemisException {};

    protected VkDevice getDevice() {
        return this.renderer.getDevice();
    }

    protected Frames getFrames() {
        return this.renderer.getFrames();
    }

    protected VkMemoryAllocator getAllocator() {
        return this.renderer.getMemoryAllocator();
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

    public void setMaterial( Material... materials ) throws ThemisException {
        setupDescriptorPool( materials );
        setupMaterials( materials );
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

    public VkDescriptorSet [] getDescriptorSet(String material, int frame ) {

        int count = 0;
        if ( this.mainDescriptorSetLayout != null ) count++;
        if ( this.descriptorSetLayout != null ) count++;

        VkDescriptorSet [] descriptorsets = new VkDescriptorSet[count];
        if ( this.descriptorSetLayout != null ) descriptorsets[--count] = getFrames().get( frame, this.fkDescriptorsets.get( material ) );
        if ( this.mainDescriptorSetLayout != null ) descriptorsets[--count] = getFrames().get( frame, this.fkMainDescriptorSet );

        return descriptorsets;

    }

    public int [] getDynamicOffset( String material, int frame ) {

        for ( int i=0; i<this.wDynamicOffset.length; i++ ) {
            this.wDynamicOffset[i] = getBackBufferDynamicOffset( material, frame, i );
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

    private void setupDescriptorPool( Material ... materials ) throws ThemisException {

        if ( this.descriptorSetLayout != null ) {
            this.descriptorPool = new VkDescriptorPool(getConfiguration(), getDevice(), getFrames().getSize() * materials.length, this.descriptorSetLayout);
            this.descriptorPool.setup();
        }

        if ( this.mainDescriptorSetLayout != null ) {
            this.mainDescriptorPool = new VkDescriptorPool(getConfiguration(), getDevice(), getFrames().getSize(), this.mainDescriptorSetLayout);
            this.mainDescriptorPool.setup();
        }

    }

    private void setupMaterials( Material ... materials ) throws ThemisException {

        if ( this.mainDescriptorSetLayout != null ) {
            getFrames().create(this.fkMainDescriptorSet, () -> new VkDescriptorSet(getConfiguration(), getDevice(), this.mainDescriptorPool, this.mainDescriptorSetLayout));
            setupMainMaterialLayout(this.fkMainDescriptorSet, materials);
        }

        if ( this.descriptorSetLayout != null ) {

            for (Material material : materials) {
                FrameKey<VkDescriptorSet> key = FrameKey.of(VkDescriptorSet.class);
                this.fkDescriptorsets.put(material.getIdentifier(), key);
                getFrames().create(key, () -> new VkDescriptorSet(getConfiguration(), getDevice(), this.descriptorPool, this.descriptorSetLayout));
                setupMaterialLayout(key, material);
            }

        }
    }

}
