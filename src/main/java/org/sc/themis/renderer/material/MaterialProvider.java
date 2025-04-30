package org.sc.themis.renderer.material;

public interface MaterialProvider {

  public <P extends MaterialRendererProperties> MaterialRenderer<P> createMaterialRenderer();

}
