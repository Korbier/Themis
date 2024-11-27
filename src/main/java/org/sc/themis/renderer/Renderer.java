package org.sc.themis.renderer;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.renderer.device.*;
import org.sc.themis.renderer.presentation.VkSurface;
import org.sc.themis.renderer.presentation.VkSwapChain;
import org.sc.themis.renderer.queue.VkQueue;
import org.sc.themis.renderer.queue.VkQueueSelectors;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.window.Window;

public class Renderer extends TObject {

    private static final Logger LOG = Logger.getLogger(Renderer.class);

    protected final static int DEFAULT_QUEUE_INDEX = 0;

    private final Window window;
    private final RendererActivity activity;


    private final VkInstance instance;
    private VkPhysicalDevice physicalDevice;
    private VkDevice device;
    private VkMemoryAllocator memoryAllocator;

    private VkSurface surface;
    private VkSwapChain swapChain;

    private VkQueue graphicQueue;
    private VkQueue transfertQueue;
    private VkQueue presentQueue;

    public Renderer(Configuration configuration, Window window, RendererActivity activity ) {
        super(configuration);
        this.window = window;
        this.activity = activity;
        this.instance = new VkInstance( configuration );
    }

    @Override
    public void setup() throws ThemisException {
        this.instance.setup();
        this.setupPhysicalDevice();
        this.setupDevice();
        this.setupMemoryAllocator();
        this.setupSurface();
        this.setupQueues();
        this.setupSwapChain();
        LOG.trace( "Renderer initialized" );
    }

    @Override
    public void cleanup() throws ThemisException {
        this.swapChain.cleanup();
        this.presentQueue.cleanup();
        this.transfertQueue.cleanup();
        this.graphicQueue.cleanup();
        this.surface.cleanup();
        this.memoryAllocator.cleanup();
        this.device.cleanup();
        this.physicalDevice.cleanup();
        this.instance.cleanup();
    }

    private void setupSwapChain() throws ThemisException {
        this.swapChain = new VkSwapChain(getConfiguration(), this.window, this.device, this.surface, this.presentQueue, this.graphicQueue, this.transfertQueue);
        this.swapChain.setup();
    }

    private void setupSurface() throws ThemisException {
        this.surface = new VkSurface(getConfiguration(), this.instance, this.window );
        this.surface.setup();
    }

    private void setupQueues() throws ThemisException {
        this.graphicQueue = this.device.selectQueue( DEFAULT_QUEUE_INDEX, VkQueueSelectors.SELECTOR_GRAPHIC_QUEUE );
        this.transfertQueue = this.device.selectQueue( DEFAULT_QUEUE_INDEX, VkQueueSelectors.SELECTOR_TRANSFERT_QUEUE );
        this.presentQueue = this.device.selectPresentQueue( DEFAULT_QUEUE_INDEX, this.surface );
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
