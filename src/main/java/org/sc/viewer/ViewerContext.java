package org.sc.viewer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F2;

import java.util.HashMap;
import java.util.Map;

import org.sc.themis.renderer.material.MaterialRenderer;
import org.sc.viewer.renderactivity.geometry.material.TextureMaterialRenderer;
import org.sc.viewer.renderactivity.postprocess.postprocessor.ShowGridPostprocessor;
import org.sc.viewer.renderactivity.postprocess.postprocessor.ShowTBNPostprocessor;

public class ViewerContext {

  private final ViewerKeyMapping keyMapping = new ViewerKeyMapping();

  private final Map<String, Boolean> postprocessors = new HashMap<>();

  private MaterialRenderer activeRenderer;

  public static ViewerContext createDefault(MaterialRenderer defaultMaterialRenderer) {

    ViewerContext context = new ViewerContext();

    context.activeRenderer = defaultMaterialRenderer;

    // Available postprocessors
    context.addPostProcessor(ShowTBNPostprocessor.IDENTIFIER);
    context.addPostProcessor(ShowGridPostprocessor.IDENTIFIER);

    // Key mapping
    context.mapPostProcessorSwitch(GLFW_KEY_F1, ShowTBNPostprocessor.IDENTIFIER);
    context.mapPostProcessorSwitch(GLFW_KEY_F2, ShowGridPostprocessor.IDENTIFIER);

    return context;
  }

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

  private void mapPostProcessorSwitch(int key, String postProcessor) {
    getKeyMapping().map(key, false, () -> switchPostProcessor(postProcessor));
  }
}
