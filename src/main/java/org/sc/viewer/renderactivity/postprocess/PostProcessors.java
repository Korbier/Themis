package org.sc.viewer.renderactivity.postprocess;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.renderpass.VkRenderPass;
import org.sc.themis.scene.descriptorset.InputDescriptorSet;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.renderactivity.postprocess.postprocessor.ShowTBNPostprocessor;

public class PostProcessors {

  private final Configuration configuration;
  private final Renderer renderer;
  private final SceneDescriptorSet sceneDescriptorSet;
  private final InputDescriptorSet geometryAttachmentDescriptorset;
  private final VkRenderPass renderpass;

  private final Map<String, PostProcessor> postprocessors = new HashMap<>();
  private final Map<String, PostProcessorPipeline> pipelines = new HashMap<>();

  public PostProcessors(
      Configuration configuration,
      Renderer renderer,
      VkRenderPass renderpass,
      SceneDescriptorSet sceneDescriptorset,
      InputDescriptorSet geometryAttachmentDescriptorset) {

    this.configuration = configuration;
    this.renderer = renderer;
    this.renderpass = renderpass;
    this.sceneDescriptorSet = sceneDescriptorset;
    this.geometryAttachmentDescriptorset = geometryAttachmentDescriptorset;

    addPostProcessor(ShowTBNPostprocessor.INSTANCE);
  }

  public void setup() throws ThemisException {
    for (PostProcessorPipeline pipeline : this.pipelines.values()) {
      pipeline.setup();
    }
  }

  public void cleanup() throws ThemisException {
    for (PostProcessorPipeline pipeline : this.pipelines.values()) {
      pipeline.cleanup();
    }
  }

  public Collection<PostProcessor> getAll() {
    return this.postprocessors.values();
  }

  public Collection<String> get(PostProcessor.Frequency frequency) {
    return this.postprocessors.values().stream()
        .filter(p -> p.getFrequency() == frequency)
        .map(PostProcessor::getIdentifier)
        .toList();
  }

  public PostProcessorPipeline getPipeline(String identifier) {
    return this.pipelines.get(identifier);
  }

  private void addPostProcessor(PostProcessor postprocessor) {
    postprocessors.put(postprocessor.getIdentifier(), postprocessor);
    pipelines.put(
        postprocessor.getIdentifier(),
        new PostProcessorPipeline(
            this.configuration, this.renderer,
            this.renderpass, this.sceneDescriptorSet,
            this.geometryAttachmentDescriptorset, postprocessor));
  }

  public void resize(
      VkRenderPass renderpass,
      SceneDescriptorSet sceneDescriptorset,
      InputDescriptorSet geometryAttachmentDescriptorset)
      throws ThemisException {
    for (PostProcessorPipeline pipeline : this.pipelines.values()) {
      pipeline.resize(renderpass, sceneDescriptorset, geometryAttachmentDescriptorset);
    }
  }
}
