package org.sc.themis.renderer;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.renderer.device.*;
import org.sc.themis.renderer.queue.VkQueue;
import org.sc.themis.renderer.queue.VkQueueSelectors;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

public class Renderer extends TObject {

    private static final Logger LOG = Logger.getLogger(Renderer.class);

    protected final static int DEFAULT_QUEUE_INDEX = 0;

    private final RendererActivity activity;
    private final VkInstance instance;
    private VkPhysicalDevice physicalDevice;
    private VkDevice device;
    private VkMemoryAllocator memoryAllocator;

    private VkQueue graphicQueue;
    private VkQueue transfertQueue;

    public Renderer(Configuration configuration, RendererActivity activity ) {
        super(configuration);
        this.activity = activity;
        this.instance = new VkInstance( configuration );
    }

    @Override
    public void setup() throws ThemisException {
        this.instance.setup();
        this.setupPhysicalDevice();
        this.setupDevice();
        this.setupMemoryAllocator();
        this.setupQueues();
        LOG.trace( "Renderer initialized" );
    }

    @Override
    public void cleanup() throws ThemisException {
        this.transfertQueue.cleanup();
        this.graphicQueue.cleanup();
        this.memoryAllocator.cleanup();
        this.device.cleanup();
        this.physicalDevice.cleanup();
        this.instance.cleanup();
    }

    private void setupQueues() throws ThemisException {
        this.graphicQueue = this.device.selectQueue( DEFAULT_QUEUE_INDEX, VkQueueSelectors.SELECTOR_GRAPHIC_QUEUE );
        this.transfertQueue = this.device.selectQueue( DEFAULT_QUEUE_INDEX, VkQueueSelectors.SELECTOR_TRANSFERT_QUEUE );
    }

    private void setupMemoryAllocator() throws ThemisException {
        this.memoryAllocator = new VkMemoryAllocator( getConfiguration(), this.physicalDevice, this.device, this.instance );
        this.memoryAllocator.setup();
    }

    private void setupDevice() throws ThemisException {
        this.device = new VkDevice( getConfiguration(), this.physicalDevice );
        this.device.setup();
    }

    private void setupPhysicalDevice() throws ThemisException {
        VkPhysicalDevices devices = new VkPhysicalDevices(getConfiguration(), this.instance);
        try {
            devices.setup();
            this.physicalDevice = devices.select(VkPhysicalDeviceSelectors.hasGraphicsQueue.and(VkPhysicalDeviceSelectors.hasKHRSwapChainExtension));
        } finally {
            devices.cleanup();
        }
    }

    public void render(Scene scene, long tpf ) {

    }

}
