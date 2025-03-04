package org.sc.viewer;

import java.util.HashMap;
import java.util.Map;

import org.sc.viewer.renderactivity.postprocess.postprocessor.ShowTBNPostprocessor;

public class ViewerContext {

    private final Map<String, Boolean> postprocessors = new HashMap<>();

    public static ViewerContext createDefault() {

        ViewerContext context = new ViewerContext();

        context.addPostProcessor(ShowTBNPostprocessor.IDENTIFIER);

        return context;

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

    public boolean isPostProcessorEnabled(String identifier) {
        return this.postprocessors.containsKey(identifier) && this.postprocessors.get(identifier);
    }

}
