package org.sc.viewer;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import org.sc.themis.renderer.material_old.MaterialRenderer;

@ApplicationScoped
public class ViewerContext {

  private final ViewerKeyMapping keyMapping = new ViewerKeyMapping();
  private final Map<String, Boolean> postprocessors = new HashMap<>();
  private MaterialRenderer activeRenderer;

  public ViewerKeyMapping getKeyMapping() {
    return this.keyMapping;
  }

  public MaterialRenderer activeRenderer() {
    return activeRenderer;
  }

  public void setActiveRenderer(MaterialRenderer activeRenderer) {
    this.activeRenderer = activeRenderer;
  }

  public void addPostProcessor(String identifier) {
    this.postprocessors.put(identifier, false);
  }

  public void enablePostProcessor(String identifier) {
    if (this.postprocessors.containsKey(identifier)) {
      this.postprocessors.put(identifier, true);
    }
  }

  public void disablePostProcessor(String identifier) {
    if (this.postprocessors.containsKey(identifier)) {
      this.postprocessors.put(identifier, false);
    }
  }

  public void switchPostProcessor(String identifier) {
    if (this.isPostProcessorEnabled(identifier)) {
      disablePostProcessor(identifier);
    } else {
      enablePostProcessor(identifier);
    }
  }

  public boolean isPostProcessorEnabled(String identifier) {
    return this.postprocessors.containsKey(identifier) && this.postprocessors.get(identifier);
  }

  public void mapPostProcessorSwitch(int key, String postProcessor) {
    getKeyMapping().map(key, false, () -> switchPostProcessor(postProcessor),() -> isPostProcessorEnabled(postProcessor));
  }
}
