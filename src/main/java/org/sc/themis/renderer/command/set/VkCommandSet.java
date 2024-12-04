package org.sc.themis.renderer.command.set;

import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.command.VkCommandBuffer;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;


public abstract class VkCommandSet extends VulkanObject {

    private final VkCommandBuffer buffer;

    public VkCommandSet(Configuration configuration, VkCommandBuffer buffer ) {
        super( configuration );
        this.buffer = buffer;
    }

    protected VkCommandBuffer buffer() {
        return this.buffer;
    }

    @Override
    public void setup() throws ThemisException {}

    @Override
    public void cleanup() throws ThemisException {}

}
