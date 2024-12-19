package org.sc.themis.scene.descriptorset;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.scene.Material;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.HashMap;
import java.util.Map;

public abstract class MaterialDescriptorSet extends VulkanObject {

    private final Renderer renderer;

    private VkDescriptorSetLayout descriptorSetLayout;
    private final Map<String, FrameKey<VkDescriptorSet>> fkDescriptorsets = new HashMap<>();
    private VkDescriptorPool descriptorPool;

    public MaterialDescriptorSet(Configuration configuration, Renderer renderer ) {
        super( configuration );
        this.renderer = renderer;
    }

    protected abstract VkDescriptorSetBinding [] getDescriptorSetBindings();
    protected abstract void setupMaterialLayout( FrameKey<VkDescriptorSet> descriptorSetKey, Material material ) throws ThemisException;
    protected abstract void cleanupMaterialLayout() throws ThemisException;

    protected Renderer getRenderer() {
        return this.renderer;
    }

    @Override
    public void setup() throws ThemisException {
        setupDescriptorLayout();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.cleanupMaterialLayout();
        this.descriptorPool.cleanup();
        this.descriptorSetLayout.cleanup();
    }

    public void setMaterial( Material... materials ) throws ThemisException {
        setupDescriptorPool( materials );
        setupMaterials( materials );
    }

    public VkDescriptorSetLayout getDescriptorSetLayout() {
        return this.descriptorSetLayout;
    }

    public VkDescriptorSet getDescriptorSet(String material, int frame ) {
        return this.renderer.getFrames().get( frame, this.fkDescriptorsets.get( material ) );
    }

    private void setupDescriptorLayout() throws ThemisException {
        this.descriptorSetLayout = new VkDescriptorSetLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            getDescriptorSetBindings()
        );
        this.descriptorSetLayout.setup();
    }

    private void setupDescriptorPool( Material ... materials ) throws ThemisException {
        this.descriptorPool = new VkDescriptorPool(getConfiguration(), this.renderer.getDevice(), this.renderer.getFrames().getSize() * materials.length, this.descriptorSetLayout );
        this.descriptorPool.setup();
    }

    private void setupMaterials( Material ... materials ) throws ThemisException {
        for ( Material material : materials ) {
            FrameKey<VkDescriptorSet> key = FrameKey.of( VkDescriptorSet.class );
            this.fkDescriptorsets.put( material.getIdentifier(), key );
            this.renderer.getFrames().create(key, () -> new VkDescriptorSet(getConfiguration(), this.renderer.getDevice(), this.descriptorPool, this.descriptorSetLayout));
            setupMaterialLayout( key, material );
        }
    }

}
