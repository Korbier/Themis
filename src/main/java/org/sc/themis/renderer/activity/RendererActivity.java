package org.sc.themis.renderer.activity;

import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.shared.Configuration;

public abstract class RendererActivity extends VulkanObject {

    public RendererActivity(Configuration configuration) {
        super(configuration);
    }

}
