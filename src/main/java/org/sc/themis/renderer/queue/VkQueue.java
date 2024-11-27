package org.sc.themis.renderer.queue;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.Objects;

public class VkQueue extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkQueue.class);

    private final org.lwjgl.vulkan.VkQueue vkQueue;

    public VkQueue( Configuration configuration, org.lwjgl.vulkan.VkQueue queue ) {
        super( configuration );
        this.vkQueue = queue;
    }

    public void setup() throws ThemisException {
    }

    @Override
    public void cleanup() throws ThemisException {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VkQueue vkQueue1 = (VkQueue) o;
        return Objects.equals(vkQueue.address(), vkQueue1.vkQueue.address());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(vkQueue.address());
    }

    public org.lwjgl.vulkan.VkQueue getHandle() {
        return this.vkQueue;
    }

}
