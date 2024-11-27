package org.sc.themis.renderer.presentation;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkInstance;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.window.Window;

import java.nio.LongBuffer;

public class VkSurface extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkSurface.class);

    private final VkInstance instance;
    private final Window window;

    private long handle;

    public VkSurface(Configuration configuration, VkInstance instance, Window window ) {
        super(configuration);
        this.instance = instance;
        this.window = window;
    }

    @Override
    public void setup() throws ThemisException {
        setupWindowSurface();
        LOG.trace("Surface initialized");
    }

    @Override
    public void cleanup() throws ThemisException {
        vkSurface().destroyWindowSurface( this.instance.getHandle(), this.handle );
    }

    public long getHandle() {
        return this.handle;
    }

    private void setupWindowSurface() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSurface = stack.mallocLong(1);
            vkSurface().createWindowSurface(this.instance.getHandle(), window.getHandle(), pSurface);
            this.handle = pSurface.get(0);
        }
    }

}
