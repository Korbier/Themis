package org.sc.themis.scene.material;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.descriptorset.MousePickingDescriptorSet;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MaterialManager extends VulkanObject {

    private final Renderer renderer;
    private final VkRenderPass renderpass;
    private final SceneDescriptorSet sceneDescriptorSet;
    private final MousePickingDescriptorSet mousePickingDescriptorSet;

    private Map<String, BaseMaterial> materials = new HashMap<>();

    public MaterialManager( Configuration configuration, Renderer renderer, VkRenderPass renderPass, SceneDescriptorSet sceneDescriptorSet, MousePickingDescriptorSet mousePickingDescriptorSet ) {
        super( configuration );
        this.renderer = renderer;
        this.renderpass = renderPass;
        this.sceneDescriptorSet = sceneDescriptorSet;
        this.mousePickingDescriptorSet = mousePickingDescriptorSet;
    }

    private void createMaterials() {
        add( new BaseColorMaterial( getConfiguration(), this.renderer, this.renderpass, this.sceneDescriptorSet ) );
    }

    public void setup( Scene scene ) throws ThemisException {

        for ( Material material : this.materials.values() ) {
            material.setup( scene );
        }

    }

    public Collection<BaseMaterial> getMaterials() {
        return this.materials.values();
    }

    @Override
    public void setup() throws ThemisException {

        createMaterials();

        for ( Material material : this.materials.values() ) {
            material.setup();
        }

    }

    @Override
    public void cleanup() throws ThemisException {
        for ( Material material : this.materials.values() ) {
            material.cleanup();
        }
    }

    private void add( BaseMaterial material ) {
        this.materials.put( material.getIdentifier(), material );
    }

}
