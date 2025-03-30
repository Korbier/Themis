package org.sc.themis.renderer.material;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.renderer.resource.model.Model;
import org.sc.themis.shared.exception.ThemisException;

/** Material manager. */
public class MaterialManager {

  private final MaterialRenderer defaultMaterialRenderer;
  private final Map<String, MaterialRenderer> availableMaterials = new HashMap<>();
  private MaterialRenderer lastUsedMaterialRenderer = null;

  /** Default constructor. */
  public MaterialManager(MaterialRenderer defaultMaterialRenderer, MaterialRenderer... materialRenderers) {
    this.defaultMaterialRenderer = defaultMaterialRenderer;
    for (MaterialRenderer materialRenderer : materialRenderers) {
      this.availableMaterials.put(materialRenderer.getIdentifier(), materialRenderer);
    }
  }

  /** Compile given material properties. */
  public void compile(Material... properties) throws ThemisException {

    for (Material materialProperties : properties) {

      this.defaultMaterialRenderer.add(materialProperties);

      for (MaterialRenderer materialRenderer : this.availableMaterials.values()) {
        materialRenderer.add(materialProperties);
      }

    }

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
  public void bindMaterialVariant(VkCommand command, Material properties, int frame)
      throws ThemisException {
    int[] indexedOffest = new int[0];
    VkDescriptorSet[] descriptorsets = this.lastUsedMaterialRenderer.getDescriptorSets(frame, properties);
    command.bindDescriptorSets(indexedOffest, descriptorsets);
  }

  private MaterialRenderer select(Model model) {
    Optional<String> oMaterialIdentifier = model.getMaterialRenderer();
    return oMaterialIdentifier.map(this.availableMaterials::get).orElse(this.defaultMaterialRenderer);
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
