package org.sc.themis.renderer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.sc.themis.core.LifeCycle;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.command.VkCommandPool;
import org.sc.themis.renderer.base.device.*;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.base.presentation.VkSurface;
import org.sc.themis.renderer.base.presentation.VkSwapChain;
import org.sc.themis.renderer.base.queue.VkQueue;
import org.sc.themis.renderer.base.queue.VkQueueSelectors;
import org.sc.themis.renderer.base.resource.image.VkImageView;
import org.sc.themis.renderer.base.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.renderer.base.sync.VkSemaphore;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.configuration.ConfigurationEnum;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.service.ServiceContainer;
import org.sc.themis.shared.utils.Timer;
import org.sc.themis.window.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Renderer implements LifeCycle {

  private static final Logger logger = LoggerFactory.getLogger(Renderer.class);

  protected static final int DEFAULT_QUEUE_INDEX = 0;

  /*** Framed object ***/
  private static final FrameKey<VkSemaphore> FK_ACQUIRE_SEMAPHORE = FrameKey.of(VkSemaphore.class);
  private static final FrameKey<VkSemaphore> FK_PRESENT_SEMAPHORE = FrameKey.of(VkSemaphore.class);

  private final Configuration configuration;

  /**
   * Renderer core objects
   **/
  private final Window window;
  private final Input input;
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
  private VkCommand transfertCommand;

  private Frames framesInFlight;
  private final Timer timer = new Timer();

  /*** Renderer service container **/
  private final ServiceContainer services = new ServiceContainer();

  boolean isSceneConfigured = false;

  public Renderer(Configuration configuration, Window window, Input input, RendererActivity activity) {
    this.window = window;
    this.input = input;
    this.activity = activity;
    this.configuration = configuration;
    this.instance = new VkInstance(configuration);
  }

  @Override
  public void setup() throws ThemisException {

    //Core setups
    this.instance.setup();
    this.setupPhysicalDevice();
    this.setupDevice();
    this.setupMemoryAllocator();

    //Presentation setup
    this.setupSurface();
    this.setupQueues();
    this.setupCommandPool();
    this.setupSwapChain();

    //Others
    this.setupResourceAllocator();

    //Frame dependent setups
    this.framesInFlight = new Frames(getFrameCount(), true, true);
    this.setupActivity();
    this.setupSemaphores();

    logger.info("\uD83C\uDFA8 Renderer initialized");
    logger.debug("⚠️ Additional information : frames in flight={}", this.getFramesInFlight().getSize());

  }

  private void setupResourceAllocator() throws ThemisException {
    getServices().set(VkStagingResourceAllocator.class, new VkStagingResourceAllocator(this.device, this.memoryAllocator));
  }

  @Override
  public void cleanup() throws ThemisException {
    this.activity.cleanup();
    this.framesInFlight.cleanup();
    this.swapChain.cleanup();
    this.transfertCommandPool.cleanup();
    this.graphicCommandPool.cleanup();
    this.presentQueue.cleanup();
    this.transfertQueue.cleanup();
    this.graphicQueue.cleanup();
    this.surface.cleanup();
    this.services.cleanup();
    this.memoryAllocator.cleanup();
    this.device.cleanup();
    this.physicalDevice.cleanup();
    this.instance.cleanup();
  }

  public ServiceContainer getServices() {
    return this.services;
  }

  public void render(Scene scene, long tpf) throws ThemisException {

    if (!this.isSceneConfigured) {
      this.configureScene(scene, false);
      this.isSceneConfigured = true;
    }

    this.timer.start("themis.renderer");

    this.getResourceAllocator().submit(this.transfertCommand);

    this.activity.render(scene, tpf);
    this.present(getPresentSemaphore(getCurrentFrame()));

    this.timer.stopAndShow();
  }

  public int acquire(Scene scene) throws ThemisException {

    try (MemoryStack stack = MemoryStack.stackPush()) {
      if (this.window.isResized() || this.swapChain.acquire(stack, getAcquireSemaphore(getCurrentFrame()))) {
        this.window.resetResized();
        this.resize(scene);
        this.swapChain.acquire(stack, getAcquireSemaphore(getCurrentFrame()));
      }
    }

    return getCurrentFrame();
  }

  public void present(VkSemaphore presentSemaphore) throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      if (this.swapChain.present(stack, presentSemaphore)) {
        this.window.setResized(true);
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
    return getServices().get(VkStagingResourceAllocator.class);
  }

  public int getFrameCount() {
    return this.swapChain.getFrameCount();
  }

  public int getCurrentFrame() {
    return this.swapChain.getCurrentFrame();
  }

  public Frames getFramesInFlight() {
    return this.framesInFlight;
  }

  public VkExtent2D getExtent() {
    return this.swapChain.getExtent();
  }

  public VkImageView getImageView(int frame) {
    return this.swapChain.getImageView(frame);
  }

  public int getImageFormat() {
    return this.swapChain.getSurfaceFormat().imageFormat();
  }

  public VkCommand createGraphicCommand(boolean primary) throws ThemisException {
    return this.graphicCommandPool.create(primary);
  }

  public VkCommand createTransfertCommand(boolean primary) throws ThemisException {
    return this.transfertCommandPool.create(primary);
  }

  public VkSemaphore getAcquireSemaphore(int frame) {
    return this.framesInFlight.get(frame, FK_ACQUIRE_SEMAPHORE);
  }

  public VkSemaphore getPresentSemaphore(int frame) {
    return this.framesInFlight.get(frame, FK_PRESENT_SEMAPHORE);
  }

  private void resize(Scene scene) throws ThemisException {

    // Recréation de la swapchain
    this.swapChain.cleanup();
    this.setupSwapChain();

    // Application de la nouvelle taille ecran a la scene (Projection)
    configureScene(scene, true);

    // Dispatch de l'evenement a l'activity
    this.activity.resize(scene);
  }

  private void configureScene(Scene scene, boolean isResizeConfiguration) throws ThemisException {

    scene.getProjection().resize(getWindow().getSize().x, getWindow().getSize().y);

    if (!isResizeConfiguration) {
      this.activity.setup(scene);
    }
  }

  private void setupMemoryAllocator() throws ThemisException {
    this.memoryAllocator = new VkMemoryAllocator(this.physicalDevice, this.device, this.instance);
    this.memoryAllocator.setup();
  }

  private void setupDevice() throws ThemisException {

    this.device = new VkDevice(
        this.physicalDevice,
        this.configuration.get(ConfigurationEnum.rendererFeatureSamplerAnisotropy, false),
        this.configuration.get(ConfigurationEnum.rendererFeatureGeometryShader, false),
        this.configuration.get(ConfigurationEnum.rendererFeatureFragmentStoresAndAtomics, false)
    );

    this.device.setup();

  }

  private void setupPhysicalDevice() throws ThemisException {
    VkPhysicalDevices devices = new VkPhysicalDevices(this.instance);
    try {
      devices.setup();
      this.physicalDevice = devices.select(VkPhysicalDeviceSelectors.hasGraphicsQueue.and(VkPhysicalDeviceSelectors.hasKHRSwapChainExtension));
    } finally {
      devices.cleanup();
    }
  }

  private void setupSwapChain() throws ThemisException {
    this.swapChain = new VkSwapChain(
        this.window, this.device, this.surface,
        this.configuration.get(ConfigurationEnum.rendererImageCount, 3),
        this.configuration.get(ConfigurationEnum.rendererVSyncEnabled, true),
        this.presentQueue, this.graphicQueue, this.transfertQueue);
    this.swapChain.setup();
  }

  private void setupSurface() throws ThemisException {
    this.surface = new VkSurface(this.instance, this.window);
    this.surface.setup();
  }

  private void setupActivity() throws ThemisException {
    this.activity.setup(this);
  }

  private void setupCommandPool() throws ThemisException {
    this.graphicCommandPool = new VkCommandPool(this.device, this.graphicQueue);
    this.graphicCommandPool.setup();
    this.transfertCommandPool = new VkCommandPool(this.device, this.transfertQueue);
    this.transfertCommandPool.setup();
    this.transfertCommand = createTransfertCommand(true);
  }

  private void setupQueues() throws ThemisException {
    this.graphicQueue = this.device.selectQueue(DEFAULT_QUEUE_INDEX, VkQueueSelectors.SELECTOR_GRAPHIC_QUEUE);
    this.transfertQueue = this.device.selectQueue(DEFAULT_QUEUE_INDEX, VkQueueSelectors.SELECTOR_TRANSFERT_QUEUE);
    this.presentQueue = this.device.selectPresentQueue(DEFAULT_QUEUE_INDEX, this.surface);
  }

  private void setupSemaphores() throws ThemisException {
    this.framesInFlight.create(FK_ACQUIRE_SEMAPHORE, () -> new VkSemaphore(this.device));
    this.framesInFlight.create(FK_PRESENT_SEMAPHORE, () -> new VkSemaphore(this.device));
  }

}
