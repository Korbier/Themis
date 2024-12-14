package org.sc.themis.renderer.command.set;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.command.VkCommandBuffer;
import org.sc.themis.renderer.exception.VulkanException;
import org.sc.themis.renderer.pipeline.VkPipeline;
import org.sc.themis.renderer.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;

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

    public void pushConstant( int shaderStage , int offset, int [] data ) throws ThemisException {
        assetPipelineBinded();
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            ByteBuffer pValues = stack.malloc(MemorySizeUtils.MAT4x4F);
            pValues.asIntBuffer().put( data );
            vkCommand().cmdPushConstants(buffer().getHandle(), pipeline().getPipelineLayout().getHandle(), shaderStage, offset, pValues);
        }
    }

    private VkPipeline pipeline() {
        return this.pipeline;
    }

    private void assetPipelineBinded() {
        if ( pipeline() == null ) throw new RuntimeException("No pipeline binded");
    }

    public void bindDescriptorSets( int[] dynamicOffsets, VkDescriptorSet... vkDescriptorSets ) throws ThemisException {

        assetPipelineBinded();

        if (vkDescriptorSets.length == 0) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush() ){

            LongBuffer pDescriptorSets = stack.mallocLong(vkDescriptorSets.length);

            int i = 0;
            for (VkDescriptorSet descriptorSet : vkDescriptorSets) {
                pDescriptorSets.put(i++, descriptorSet.getHandle());
            }

            IntBuffer dynOffset = null;

            if (dynamicOffsets.length > 0) {
                dynOffset = stack.mallocInt(dynamicOffsets.length);
                for (int j = 0; j < dynamicOffsets.length; j++) {
                    dynOffset.put(j, dynamicOffsets[j]);
                }
            }

            vkPipeline().cmdBindDescriptorSets( buffer().getHandle(), VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline().getPipelineLayout().getHandle(), 0, pDescriptorSets, dynOffset );

        }

    }
}
