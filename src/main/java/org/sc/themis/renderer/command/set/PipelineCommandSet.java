package org.sc.themis.renderer.command.set;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.command.VkCommandBuffer;
import org.sc.themis.renderer.pipeline.VkPipeline;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.nio.ByteBuffer;

public class PipelineCommandSet extends VkCommandSet {

    private VkPipeline pipeline;

    public PipelineCommandSet(Configuration configuration, VkCommandBuffer buffer) {
        super(configuration, buffer);
    }

    public void bindPipeline( VkPipeline pipeline, int pipelineBindPoint ) throws ThemisException {
        this.pipeline = pipeline;
        vkCommand().cmdBindPipeline( buffer().getHandle(), pipelineBindPoint, pipeline.getHandle() );
    }

    public void pushConstant( int shaderStage , int offset, float [] data ) throws ThemisException {
        assetPipelineBinded();
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            ByteBuffer pValues = stack.malloc(MemorySizeUtils.MAT4x4F);
            pValues.asFloatBuffer().put( data );
            vkCommand().cmdPushConstants(buffer().getHandle(), pipeline().getPipelineLayout().getHandle(), shaderStage, offset, pValues);
        }
    }

    private VkPipeline pipeline() {
        return this.pipeline;
    }

    private void assetPipelineBinded() {
        if ( pipeline() == null ) throw new RuntimeException("No pipeline binded");
    }

}
