package org.sc.viewer.renderactivity.postprocess.pipeline;

import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.renderpass.VkRenderPass;
import org.sc.themis.scene.descriptorset.InputDescriptorSet;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.LifeCycle;

public interface PostProcessorPipeline extends LifeCycle {

  void resize(VkRenderPass renderpass, SceneDescriptorSet sceneDescriptorset, InputDescriptorSet geometryAttachmentDescriptorset) throws ThemisException;
  void bind(VkCommand command, int frame) throws ThemisException;

}
