package org.sc.viewer.renderactivity.postprocess;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.renderpass.VkRenderPass;
import org.sc.themis.scene.descriptorset.InputDescriptorSet;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PostProcessors {

    private final Configuration configuration;
    private final Renderer renderer;
    private final SceneDescriptorSet sceneDescriptorSet;
    private final InputDescriptorSet geometryAttachmentDescriptorset;
    private final VkRenderPass renderpass;

    private final Map<String, PostProcessor> postprocessors = new HashMap<>();
    private final Map<String, Boolean> enabled = new HashMap<>();
    private final Map<String, PostProcessorPipeline> pipelines = new HashMap<>();

    public PostProcessors(Configuration configuration, Renderer renderer, VkRenderPass renderpass, SceneDescriptorSet sceneDescriptorset, InputDescriptorSet geometryAttachmentDescriptorset) {
        this.configuration = configuration;
        this.renderer = renderer;
        this.renderpass = renderpass;
        this.sceneDescriptorSet = sceneDescriptorset;
        this.geometryAttachmentDescriptorset = geometryAttachmentDescriptorset;
    }

    public void addPostProcessor( PostProcessor postprocessor, boolean bEnabled ) {
        postprocessors.put( postprocessor.getIdentifier(), postprocessor );
        enabled.put( postprocessor.getIdentifier(), bEnabled );
        pipelines.put( postprocessor.getIdentifier(), new PostProcessorPipeline( this.configuration, this.renderer, this.renderpass, this.sceneDescriptorSet, this.geometryAttachmentDescriptorset, postprocessor ) );
    }

    public void setup() throws ThemisException {
        for ( PostProcessorPipeline pipeline : this.pipelines.values() ) {
            pipeline.setup();
        }
    }

    public void cleanup() throws ThemisException {
        for ( PostProcessorPipeline pipeline : this.pipelines.values() ) {
            pipeline.cleanup();
        }
    }

    public void enablePostProcessor( String identifier ) {
        enabled.put( identifier, true );
    }

    public void disablePostProcessor( String identifier ) {
        enabled.put( identifier, false );
    }

    public Collection<PostProcessor> getAll() {
        return this.postprocessors.values();
    }

    public Collection<PostProcessorPipeline> getEnabled(PostProcessor.Frequency frequency) {
        return this.postprocessors
                .values()
                .stream()
                .filter( p -> p.getFrequency() == frequency && enabled.get( p.getIdentifier() ) )
                .map( p -> pipelines.get( p.getIdentifier() ) )
                .toList();
    }

}
