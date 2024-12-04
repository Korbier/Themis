package org.sc.themis.renderer.command.set;

import org.sc.themis.renderer.command.VkCommandBuffer;
import org.sc.themis.renderer.pipeline.VkPipeline;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public class PipelineCommandSet extends VkCommandSet {

    private VkPipeline pipeline;

    public PipelineCommandSet(Configuration configuration, VkCommandBuffer buffer) {
        super(configuration, buffer);
    }

    public void bindPipeline( VkPipeline pipeline, int pipelineBindPoint ) throws ThemisException {
        this.pipeline = pipeline;
        vkCommand().cmdBindPipeline( buffer().getHandle(), pipelineBindPoint, pipeline.getHandle() );
    }

    private VkPipeline pipeline() {
        return this.pipeline;
    }

    private void assetPipelineBinded() {
        if ( pipeline() == null ) throw new RuntimeException("No pipeline binded");
    }

}
