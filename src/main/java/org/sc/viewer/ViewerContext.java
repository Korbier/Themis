package org.sc.viewer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;

import java.util.HashMap;
import java.util.Map;
import org.sc.viewer.renderactivity.postprocess.postprocessor.ShowTBNPostprocessor;


public class ViewerContext {

    private final ViewerKeyMapping keyMapping = new ViewerKeyMapping();
    private final Map<String, Boolean> postprocessors = new HashMap<>();

    public static ViewerContext createDefault() {

        ViewerContext context = new ViewerContext();

        //Available postprocessors
        context.addPostProcessor(ShowTBNPostprocessor.IDENTIFIER);

        //Key mapping
        context.mapPostProcessorSwitch(GLFW_KEY_F1, ShowTBNPostprocessor.IDENTIFIER);

        return context;

    }

    public ViewerKeyMapping getKeyMapping() {
        return this.keyMapping;
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
