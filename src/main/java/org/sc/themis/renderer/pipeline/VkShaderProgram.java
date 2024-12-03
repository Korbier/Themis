package org.sc.themis.renderer.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;

public class VkShaderProgram extends VulkanObject {

    private final VkDevice device;
    private final VkShaderProgramStage[] stages;
    private final Map<Integer, Long> handles = new HashMap<>();

    public VkShaderProgram(Configuration configuration, VkDevice device, VkShaderProgramStage ... stages ) {
        super(configuration);
        this.device = device;
        this.stages = stages;
    }

    @Override
    public void setup() throws ThemisException {

        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.handles.clear();
            for (VkShaderProgramStage stage : this.stages) {
                if ( stage != null ) {
                    long handle = vkCreateShaderModule(stack, stage);
                    this.handles.put(stage.shaderStage(), handle);
                }
            }
        }

    }

    @Override
    public void cleanup() throws ThemisException {
        for ( Long handle : this.handles.values() ) {
            vkPipeline().destroyShaderModule( this.device.getHandle(), handle );
        }
    }

    public Map<Integer, Long> handles() {
        return this.handles;
    }

    public int size() {
        return this.handles.size();
    }

    private long vkCreateShaderModule(MemoryStack stack, VkShaderProgramStage stage) throws ThemisException {

        VkShaderModuleCreateInfo shaderModuleCreateInfo = createShaderModuleCreateInfo( stack, stage.source() );
        LongBuffer pShaderModule = stack.mallocLong(1);

        vkPipeline().createShaderModule( this.device.getHandle(), shaderModuleCreateInfo, pShaderModule );

        return pShaderModule.get( 0 );

    }

    private VkShaderModuleCreateInfo createShaderModuleCreateInfo(MemoryStack stack, byte [] source) {

        ByteBuffer pSourceContent = stack.malloc(source.length).put(0, source);

        return VkShaderModuleCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                .pCode(pSourceContent);

    }

}
