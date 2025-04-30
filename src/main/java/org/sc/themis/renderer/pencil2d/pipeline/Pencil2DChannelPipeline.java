package org.sc.themis.renderer.pencil2d.pipeline;

import org.sc.themis.renderer.base.pipeline.VkPipeline;
import org.sc.themis.core.LifeCycle;

public interface Pencil2DChannelPipeline extends LifeCycle {
  public VkPipeline getPipeline();
}
