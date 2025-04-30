package org.sc.themis.renderer.material;

import jakarta.enterprise.context.ApplicationScoped;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.renderer.resource.model.Mesh;
import org.sc.themis.renderer.resource.model.Model;
import org.sc.themis.shared.exception.ThemisException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class MaterialService {

  private MaterialRenderer<?> defaultMaterialRenderer;
  private Set<MaterialRenderer<?>> materialRenderers = new HashSet<>();
  private Map<Model, MaterialRenderer<?>> modelMapping = new HashMap<>();
  private Map<MaterialRenderer<?>, Map<Material, MaterialVariant<Material>>> materialMapping = new HashMap<>();

  public <P extends MaterialRendererProperties> void register(MaterialRenderer<P> materialRenderer, boolean defaultRenderer) {

    this.materialRenderers.add(materialRenderer);

    if (defaultRenderer) {
      this.defaultMaterialRenderer = materialRenderer;
    }

  }

  public MaterialRenderer<?> getMaterialRenderer(Model model) {

    if (this.modelMapping.containsKey(model)) {
      return this.modelMapping.get(model);
    }

    return this.defaultMaterialRenderer;

  }

  public MaterialVariant<Material> getMaterial(Renderer renderer, MaterialRenderer<?> materialRenderer, Mesh mesh) throws ThemisException {

    Material material = mesh.getMaterial();

    if (!this.materialMapping.containsKey(materialRenderer)) {
      this.materialMapping.put(materialRenderer, new HashMap<>());
    }

    Map<Material, MaterialVariant<Material>> variants = this.materialMapping.get(materialRenderer);

    if (!variants.containsKey(material)) {
      MaterialVariant<Material> variant = materialRenderer.create(renderer, material);
      variants.put(material, variant);
      return variant;
    }

    return variants.get(material);

  }

}
