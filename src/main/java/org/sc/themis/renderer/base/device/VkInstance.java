package org.sc.themis.renderer.base.device;

import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
import static org.lwjgl.vulkan.VK10.VK_FALSE;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK13.VK_API_VERSION_1_3;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.sc.themis.renderer.base.device.extension.VkDefaultExtensions;
import org.sc.themis.renderer.base.device.extension.VkExtension;
import org.sc.themis.renderer.base.device.extension.VkExtensions;
import org.sc.themis.renderer.base.device.layer.VkDefaultLayers;
import org.sc.themis.renderer.base.device.layer.VkLayer;
import org.sc.themis.renderer.base.device.layer.VkLayers;
import org.sc.themis.renderer.lang.VulkanObject;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.configuration.ConfigurationEnum;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.LogUtils;
import org.slf4j.LoggerFactory;

public class VkInstance extends VulkanObject {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VkInstance.class);

  private static final int MESSAGE_SEVERITY_BITMASK =
      VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT
      | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;

  private static final int MESSAGE_TYPE_BITMASK =
      VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
      | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
      | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;

  private final VkLayers layers;
  private final VkExtensions extensions;
  private final List<VkLayer> validationLayers = new ArrayList<>();

  private VkDebugUtilsMessengerCreateInfoEXT vkDebugMessenger = null;
  private long debugMessengerHandler = VK_NULL_HANDLE;

  private boolean vkDebugEnabled = false;

  private org.lwjgl.vulkan.VkInstance handle;

  public VkInstance(Configuration configuration) {
    super(configuration);
    this.layers = new VkLayers(configuration);
    this.extensions = new VkExtensions(configuration);
  }

  @Override
  public void setup() throws ThemisException {
    setupInnerObjects();
    setupDebugMode();
    setupVkInstance();
    attachDebugMessengerToInstance();
    logger.trace("Instance initialized (handle={}) [debug={}]", LogUtils.toHexString(this.handle.address()), this.vkDebugEnabled);
  }

  @Override
  public void cleanup() throws ThemisException {
    vkCleanupDebugMessenger();
    vkCleanupInstance();
    cleanupInnerObjects();
  }

  private void cleanupInnerObjects() {
    getLayers().cleanup();
    getExtensions().cleanup();
    getValidationLayers().clear();
  }

  private void vkCleanupInstance() throws ThemisException {
    vkInstance().destroyInstance(this.handle);
    this.handle = null;
  }

  private void vkCleanupDebugMessenger() throws ThemisException {

    if (this.debugMessengerHandler != VK_NULL_HANDLE) {
      vkDebug().destroyDebugUtilsMessengerEXT(this.handle, this.debugMessengerHandler);
      this.debugMessengerHandler = VK_NULL_HANDLE;
    }

    if (this.vkDebugMessenger != null) {
      this.vkDebugMessenger.pfnUserCallback().free();
      this.vkDebugMessenger.free();
    }

  }

  public boolean isDebugEnabled() {
    return this.vkDebugEnabled;
  }

  public VkLayers getLayers() {
    return this.layers;
  }

  public List<VkLayer> getValidationLayers() {
    return this.validationLayers;
  }

  public VkExtensions getExtensions() {
    return this.extensions;
  }

  public org.lwjgl.vulkan.VkInstance getHandle() {
    return this.handle;
  }

  public long getDebugMessengerHandle() {
    return this.debugMessengerHandler;
  }

  private void setupInnerObjects() throws ThemisException {
    this.layers.setup();
    this.extensions.setup();
  }

  private void setupDebugMode() throws ThemisException {
    this.vkDebugEnabled = checkDebugMode();
    this.vkDebugMessenger = this.vkDebugEnabled ? createDebugMessenger() : null;
    logger.trace("[VkInstance] Debug mode enabled : {}", this.vkDebugEnabled);
  }

  private void setupVkInstance() throws ThemisException {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      PointerBuffer extensions = selectVkExtensions(stack);
      PointerBuffer layers = selectVkLayers(stack);
      VkInstanceCreateInfo instanceCreateInfo = createInstanceCreateInfo(stack, extensions, layers);
      this.handle = vkCreateInstance(stack, instanceCreateInfo);
    }
  }

  protected VkInstanceCreateInfo createInstanceCreateInfo(MemoryStack stack, PointerBuffer extensions, PointerBuffer layers) throws ThemisException {

    VkApplicationInfo applicationInfo = createApplicationInfo(stack);

    return VkInstanceCreateInfo.calloc(stack)
        .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
        .pNext(this.vkDebugMessenger != null ? this.vkDebugMessenger.address() : MemoryUtil.NULL)
        .pApplicationInfo(applicationInfo)
        .ppEnabledLayerNames(layers)
        .ppEnabledExtensionNames(extensions);

  }

  private VkApplicationInfo createApplicationInfo(MemoryStack stack) {
    return VkApplicationInfo.calloc(stack)
        .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
        .pApplicationName(stack.UTF8(getConfiguration().get(ConfigurationEnum.applicationName, "no-name")))
        .applicationVersion(getConfiguration().get(ConfigurationEnum.applicationVersion, 1))
        .pEngineName(stack.UTF8(getConfiguration().get(ConfigurationEnum.engineName, "no-name")))
        .engineVersion(getConfiguration().get(ConfigurationEnum.engineVersion, 1))
        .apiVersion(VK_API_VERSION_1_3);
  }

  private PointerBuffer selectVkExtensions(MemoryStack stack) {

    PointerBuffer extGlfw = this.fetchGlfwExtensions();
    VkExtension extDebug = this.fetchDebugExtensions();

    boolean debug = this.isDebugEnabled();

    int extensionCount = 0;
    extensionCount += extGlfw.remaining();
    if (debug) extensionCount += 1;

    PointerBuffer extensions = stack.mallocPointer(extensionCount);
    extensions.put(extGlfw);
    if (debug) extensions.put(stack.UTF8(extDebug.getName()));

    extensions.flip();

    return extensions;

  }

  private PointerBuffer selectVkLayers(MemoryStack stack) {

    PointerBuffer requiredLayers = stack.mallocPointer(getValidationLayers().size());

    for (int i = 0; i < getValidationLayers().size(); i++) {
      requiredLayers.put(i, stack.ASCII(getValidationLayers().get(i).getName()));
    }

    return requiredLayers;

  }

  private PointerBuffer fetchGlfwExtensions() {
    return GLFWVulkan.glfwGetRequiredInstanceExtensions();
  }

  private VkExtension fetchDebugExtensions() {
    return VkDefaultExtensions.EXT_DEBUG_UTILS_EXTENSION_NAME;
  }

  private boolean checkDebugMode() {

    if (!getConfiguration().get(ConfigurationEnum.rendererDebug, false)) {
      return false;
    }

    getValidationLayers().clear();

    getValidationLayers().addAll(this.layers.filter(VkDefaultLayers.KHRONOS_VALIDATION));

    if (getValidationLayers().isEmpty()) {
      getValidationLayers().addAll(this.layers.filter(VkDefaultLayers.LUNARG_STANDARD_VALIDATION));
    }

    if (getValidationLayers().isEmpty()) {
      getValidationLayers()
          .addAll(
              this.layers.filter(
                  VkDefaultLayers.GOOGLE_THREADING,
                  VkDefaultLayers.LUNARG_PARAMETER_VALIDATION,
                  VkDefaultLayers.LUNARG_CORE_VALIDATION,
                  VkDefaultLayers.LUNARG_OBJECT_VALIDATION,
                  VkDefaultLayers.LUNARG_UNIQUE_VALIDATION));
    }

    return !getValidationLayers().isEmpty();

  }

  private VkDebugUtilsMessengerCreateInfoEXT createDebugMessenger() {

    return VkDebugUtilsMessengerCreateInfoEXT.calloc()
        .sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
        .messageSeverity(MESSAGE_SEVERITY_BITMASK)
        .messageType(MESSAGE_TYPE_BITMASK)
        .pfnUserCallback(VkInstance::debugCallback);

  }

  private void attachDebugMessengerToInstance() throws ThemisException {

    if (this.isDebugEnabled()) {

      try (MemoryStack stack = MemoryStack.stackPush()) {
        LongBuffer buffer = stack.mallocLong(1);
        vkDebug().createDebugUtilsMessengerEXT(this.handle, this.vkDebugMessenger, buffer);
        this.debugMessengerHandler = buffer.get(0);
      }

    }

  }

  private static int debugCallback(int messageSeverity, int messageTypes, long pCallbackData, long pUserData) {

    VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

    if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT) != 0) {
      logger.info("[Vulkan Debug] {}", callbackData.pMessageString());
    } else if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) != 0) {
      logger.warn("[Vulkan Debug] {}", callbackData.pMessageString());
    } else if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0) {
      logger.error("[Vulkan Debug] {}", callbackData.pMessageString());
    } else {
      logger.debug("[Vulkan Debug] {}", callbackData.pMessageString());
    }

    return VK_FALSE;

  }

  private org.lwjgl.vulkan.VkInstance vkCreateInstance(MemoryStack stack, VkInstanceCreateInfo instanceCreateInfo) throws ThemisException {
    PointerBuffer pInstance = stack.mallocPointer(1);
    vkInstance().createInstance(instanceCreateInfo, pInstance);
    return new org.lwjgl.vulkan.VkInstance(pInstance.get(0), instanceCreateInfo);
  }
}
