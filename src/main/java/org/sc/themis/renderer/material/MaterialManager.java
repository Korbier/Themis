package org.sc.themis.renderer.material;

import java.util.*;

import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.renderer.resource.model.Model;
import org.sc.themis.shared.exception.ThemisException;

/** Material manager. */
public class MaterialManager {

  private final Map<String, MaterialRenderer> availableMaterials = new HashMap<>();
  private MaterialRenderer lastUsedMaterialRenderer = null;

  private final List<Material> materials = new ArrayList<>();

  public void setMaterialRenderers(MaterialRenderer defaultMaterialRenderer, MaterialRenderer... materialRenderers) {
    for (MaterialRenderer materialRenderer : materialRenderers) {
      this.availableMaterials.put(materialRenderer.getIdentifier(), materialRenderer);
    }
  }

  public MaterialRenderer get(String id) {
    return this.availableMaterials.get(id);
  }

  public void addMaterials(Material... materials) throws ThemisException {
    this.materials.addAll(Arrays.asList(materials));
    if (!this.availableMaterials.isEmpty()) {
      compile();
    }
  }

  /** Compile given material properties. */
  public void compile() throws ThemisException {
    for (Material materialProperties : this.materials) {
      for (MaterialRenderer materialRenderer : this.availableMaterials.values()) {
        materialRenderer.add(materialProperties);
      }
    }
    this.materials.clear();;
  }

  /** Bind material pipeline for given model. */
  public void bindMaterial(VkCommand command, Model model) throws ThemisException {

    MaterialRenderer materialRenderer = select(model);

    if (this.lastUsedMaterialRenderer == null || !this.lastUsedMaterialRenderer.equals(materialRenderer)) {
      this.lastUsedMaterialRenderer = materialRenderer;
    }

    command.bindPipeline(this.lastUsedMaterialRenderer.getPipeline());

  }

  /** Bind material variant (descriptorset) for given material properties. */
  public void bindMaterialVariant(VkCommand command, Material properties, int frame) throws ThemisException {
    int[] indexedOffest = new int[0];
    this.lastUsedMaterialRenderer.update(frame, properties);
    VkDescriptorSet[] descriptorsets = this.lastUsedMaterialRenderer.getDescriptorSets(frame, properties);
    command.bindDescriptorSets(indexedOffest, descriptorsets);
  }

  private MaterialRenderer select(Model model) {
    Optional<String> oMaterialIdentifier = model.getMaterialRenderer();
    return oMaterialIdentifier.map(this.availableMaterials::get).orElseThrow();
  }

  /** Select material properties to use for current material. */
  public Material select(Material... properties) {

    for (Material material : properties) {
      if (material.getVariantIdentifier(this.lastUsedMaterialRenderer) != null) {
        return material;
      }
    }

    return null;

  }
  
}
