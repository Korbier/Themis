package org.sc.themis.renderer;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.command.VkCommandPool;
import org.sc.themis.renderer.device.*;
import org.sc.themis.renderer.presentation.VkSurface;
import org.sc.themis.renderer.presentation.VkSwapChain;
import org.sc.themis.renderer.queue.VkQueue;
import org.sc.themis.renderer.queue.VkQueueSelectors;
import org.sc.themis.renderer.resource.image.VkImageView;
import org.sc.themis.renderer.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.renderer.sync.VkSemaphore;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.window.Window;

public class Renderer extends TObject {

    private static final Logger LOG = Logger.getLogger(Renderer.class);

    protected final static int DEFAULT_QUEUE_INDEX = 0;

    /*** Framed object ***/
    private final static FrameKey<VkSemaphore> FK_ACQUIRE_SEMAPHORE = FrameKey.of( VkSemaphore.class );
    private final static FrameKey<VkSemaphore> FK_PRESENT_SEMAPHORE = FrameKey.of( VkSemaphore.class );

    private final Window window;
    private final Input input;
    private final RendererActivity activity;

    private final VkInstance instance;
    private VkPhysicalDevice physicalDevice;
    private VkDevice device;
    private VkMemoryAllocator memoryAllocator;
    private VkStagingResourceAllocator resourceAllocator;

    private VkSurface surface;
    private VkSwapChain swapChain;

    private VkQueue graphicQueue;
    private VkQueue transfertQueue;
    private VkQueue presentQueue;

    private VkCommandPool graphicCommandPool;
    private VkCommandPool transfertCommandPool;

    private Frames frames;

    boolean isSceneConfigured = false;

    public Renderer(Configuration configuration, Window window, Input input, RendererActivity activity ) {
        super(configuration);
        this.window = window;
        this.input = input;
        this.activity = activity;
        this.instance = new VkInstance( configuration );
    }

    @Override
    public void setup() throws ThemisException {

        /** Core setups **/
        this.instance.setup();
        this.setupPhysicalDevice();
        this.setupDevice();
        this.setupMemoryAllocator();

        /** Presentation setup **/
        this.setupSurface();
        this.setupQueues();
        this.setupCommandPool();
        this.setupSwapChain();

        /** Others **/
        this.setupResourceAllocator();

        /** Frame dependent setups **/
        this.frames = new Frames( getFrameCount(), true, true );
        this.setupActivity();
        this.setupSemaphores();

        LOG.trace( "Renderer initialized" );

    }

    private void setupResourceAllocator() throws ThemisException {
        this.resourceAllocator = new VkStagingResourceAllocator( getConfiguration(), this.device, this.memoryAllocator, createTransfertCommand( true ));
        this.resourceAllocator.setup();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.activity.cleanup();
        this.frames.cleanup();
        this.swapChain.cleanup();
        this.transfertCommandPool.cleanup();
        this.graphicCommandPool.cleanup();
        this.presentQueue.cleanup();
        this.transfertQueue.cleanup();
        this.graphicQueue.cleanup();
        this.surface.cleanup();
        this.resourceAllocator.cleanup();
        this.memoryAllocator.cleanup();
        this.device.cleanup();
        this.physicalDevice.cleanup();
        this.instance.cleanup();
    }

    public void render( Scene scene, long tpf ) throws ThemisException {

        if ( !this.isSceneConfigured ) {
            this.configureScene( scene, false );
            this.isSceneConfigured = true;
        }

        this.getResourceAllocator().commit();
        this.activity.render( scene, tpf );
        this.present( getPresentSemaphore( getCurrentFrame() ) );
    }

    public int acquire( Scene scene ) throws ThemisException {

        try (MemoryStack stack = MemoryStack.stackPush() ) {
            if (this.window.isResized() || this.swapChain.acquire(stack, getAcquireSemaphore( getCurrentFrame() ) ) ) {
                this.window.resetResized();
                this.resize( scene );
                this.swapChain.acquire(stack, getAcquireSemaphore( getCurrentFrame() ) );
            }
        }

        return getCurrentFrame();

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

    public Window getWindow() {
        return this.window;
    }

    public Input getInput() {
        return this.input;
    }

    public VkDevice getDevice() {
        return this.device;
    }

    public VkMemoryAllocator getMemoryAllocator() {
        return this.memoryAllocator;
    }

    public VkStagingResourceAllocator getResourceAllocator() {
        return this.resourceAllocator;
    }

    public int getFrameCount() {
        return this.swapChain.getFrameCount();
    }

    private int getCurrentFrame() {
        return this.swapChain.getCurrentFrame();
    }

    public Frames getFrames() {
        return this.frames;
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

    public VkSemaphore getAcquireSemaphore(int frame ) {
        return this.frames.get( frame, FK_ACQUIRE_SEMAPHORE );
    }

    public VkSemaphore getPresentSemaphore( int frame ) {
        return this.frames.get( frame, FK_PRESENT_SEMAPHORE );
    }

    private void resize(Scene scene) throws ThemisException {

        //Recréation de la swapchain
        this.swapChain.cleanup();
        this.setupSwapChain();

        //Application de la nouvelle taille ecran a la scene (Projection)
        configureScene(scene, true);

        //Dispatch de l'evenement a l'activity
        this.activity.resize();
    }

    private void configureScene( Scene scene, boolean isResizeConfiguration ) throws ThemisException {

        scene.getProjection().resize( getWindow().getSize().x, getWindow().getSize().y );

        if ( !isResizeConfiguration ) {
            this.activity.setup( scene );
        }

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
        this.frames.create( FK_ACQUIRE_SEMAPHORE, () -> new VkSemaphore(getConfiguration(), this.device) );
        this.frames.create( FK_PRESENT_SEMAPHORE, () -> new VkSemaphore(getConfiguration(), this.device) );
    }

}
