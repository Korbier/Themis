package org.sc.themis.renderer.material;

import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.scene.base.geometry.Model;
import org.sc.themis.shared.exception.ThemisException;

/** Material manager. */
public class MaterialManager {

  private final Material defaultMaterial;
  private Material lastUsedMaterial = null;

  /** Default constructor. */
  public MaterialManager(Material defaultMaterial) {
    this.defaultMaterial = defaultMaterial;
  }

  /** Compile given material properties. */
  public void compile(MaterialProperties... properties) throws ThemisException {
    for (MaterialProperties materialProperties : properties) {
      this.defaultMaterial.add(materialProperties);
    }
  }

  /** Bind material pipeline for given model. */
  public void bindMaterial(VkCommand command, Model model) throws ThemisException {

    Material material = select(model);

    if (this.lastUsedMaterial == null || !this.lastUsedMaterial.equals(material)) {
      this.lastUsedMaterial = material;
    }

    command.bindPipeline(this.lastUsedMaterial.getPipeline());
  }

  /** Bind material variant (descriptorset) for given material properties. */
  public void bindMaterialVariant(VkCommand command, MaterialProperties properties, int frame)
      throws ThemisException {
    int[] indexedOffest = new int[0];
    VkDescriptorSet[] descriptorsets = this.lastUsedMaterial.getDescriptorSets(frame, properties);
    command.bindDescriptorSets(indexedOffest, descriptorsets);
  }

  private Material select(Model model) {
    return this.defaultMaterial;
  }

  /** Select material properties to use for current material. */
  public MaterialProperties select(MaterialProperties... properties) {

    for (MaterialProperties materialProperties : properties) {
      if (materialProperties.getVariantIdentifier(this.lastUsedMaterial) != null) {
        return materialProperties;
      }
    }

    return null;
  }
}
