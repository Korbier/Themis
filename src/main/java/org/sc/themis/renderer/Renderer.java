package org.sc.themis.renderer;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.command.VkCommandPool;
import org.sc.themis.renderer.device.*;
import org.sc.themis.renderer.presentation.VkSurface;
import org.sc.themis.renderer.presentation.VkSwapChain;
import org.sc.themis.renderer.queue.VkQueue;
import org.sc.themis.renderer.queue.VkQueueSelectors;
import org.sc.themis.renderer.resource.image.VkImageView;
import org.sc.themis.renderer.sync.VkSemaphore;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.shared.utils.FramedObject;
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

    private VkCommandPool graphicCommandPool;
    private VkCommandPool transfertCommandPool;

    private FramedObject<VkSemaphore> acquireSemaphore;
    private FramedObject<VkSemaphore> presentSemaphore;

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
        this.setupCommandPool();
        this.setupSwapChain();
        this.setupSemaphores();
        this.setupActivity();
        LOG.trace( "Renderer initialized" );
    }

    @Override
    public void cleanup() throws ThemisException {
        this.activity.cleanup();
        this.presentSemaphore.accept( VkSemaphore::cleanup );
        this.acquireSemaphore.accept( VkSemaphore::cleanup );
        this.swapChain.cleanup();
        this.transfertCommandPool.cleanup();
        this.graphicCommandPool.cleanup();
        this.presentQueue.cleanup();
        this.transfertQueue.cleanup();
        this.graphicQueue.cleanup();
        this.surface.cleanup();
        this.memoryAllocator.cleanup();
        this.device.cleanup();
        this.physicalDevice.cleanup();
        this.instance.cleanup();
    }

    public void render( Scene scene, long tpf ) throws ThemisException {
        this.activity.render( scene, tpf );
        this.present( getPresentSemaphore( getCurrentFrame() ) );
    }

    public void acquire() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            if (this.window.isResized() || this.swapChain.acquire(stack, getAcquireSemanphore( getCurrentFrame() ) ) ) {
                this.window.resetResized();
                this.resize();
                this.swapChain.acquire(stack, getAcquireSemanphore( getCurrentFrame() ) );
            }
        }
    }

    public void present( VkSemaphore presentSemaphore ) throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            if ( this.swapChain.present( stack, presentSemaphore ) ) {
                this.window.setResized( true );
            }
        }
    }

    public void waitIdle() throws ThemisException {
        getDevice().waitIdle();
    }

    public VkDevice getDevice() {
        return this.device;
    }

    public VkMemoryAllocator getMemoryAllocator() {
        return this.memoryAllocator;
    }

    public int getFrameCount() {
        return this.swapChain.getFrameCount();
    }

    public int getCurrentFrame() {
        return this.swapChain.getCurrentFrame();
    }

    public VkExtent2D getExtent() {
        return this.swapChain.getExtent();
    }

    public VkImageView getImageView( int frame ) {
        return this.swapChain.getImageView( frame );
    }

    public int getImageFormat() {
        return this.swapChain.getSurfaceFormat().imageFormat();
    }

    public VkCommand createGraphicCommand( boolean primary ) throws ThemisException {
        return this.graphicCommandPool.create( primary );
    }

    public VkCommand createTransfertCommand( boolean primary ) throws ThemisException {
        return this.transfertCommandPool.create( primary );
    }

    public VkSemaphore getAcquireSemanphore( int frame ) {
        return this.acquireSemaphore.get( frame );
    }

    public VkSemaphore getPresentSemaphore( int frame ) {
        return this.presentSemaphore.get( frame );
    }

    private void resize() throws ThemisException {
        this.activity.resize();
    }

    private void setupSwapChain() throws ThemisException {
        this.swapChain = new VkSwapChain(getConfiguration(), this.window, this.device, this.surface, this.presentQueue, this.graphicQueue, this.transfertQueue);
        this.swapChain.setup();
    }

    private void setupSurface() throws ThemisException {
        this.surface = new VkSurface(getConfiguration(), this.instance, this.window );
        this.surface.setup();
    }

    private void setupActivity() throws ThemisException {
        this.activity.setup( this );
    }

    private void setupCommandPool() throws ThemisException {
        this.graphicCommandPool = new VkCommandPool( getConfiguration(), this.device, this.graphicQueue );
        this.graphicCommandPool.setup();
        this.transfertCommandPool = new VkCommandPool( getConfiguration(), this.device, this.transfertQueue );
        this.transfertCommandPool.setup();
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

    private void setupSemaphores() throws ThemisException {
        this.acquireSemaphore = FramedObject.of( this.swapChain.getFrameCount(), () -> {
            VkSemaphore semaphore = new VkSemaphore(getConfiguration(), this.device);
            semaphore.setup();
            return semaphore;
        });
        this.presentSemaphore = FramedObject.of( this.swapChain.getFrameCount(), () -> {
            VkSemaphore semaphore = new VkSemaphore(getConfiguration(), this.device);
            semaphore.setup();
            return semaphore;
        });
    }

}
