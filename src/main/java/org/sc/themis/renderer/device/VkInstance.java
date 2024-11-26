package org.sc.themis.renderer.device;

import org.jboss.logging.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.sc.themis.renderer.base.VkObject;
import org.sc.themis.renderer.device.extension.VkDefaultExtensions;
import org.sc.themis.renderer.device.extension.VkExtension;
import org.sc.themis.renderer.device.extension.VkExtensions;
import org.sc.themis.renderer.device.layer.VkDefaultLayers;
import org.sc.themis.renderer.device.layer.VkLayer;
import org.sc.themis.renderer.device.layer.VkLayers;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK13.VK_API_VERSION_1_3;

public class VkInstance extends VkObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkInstance.class);

    private static final int MESSAGE_SEVERITY_BITMASK = VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
    private static final int MESSAGE_TYPE_BITMASK     = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;

    final private VkLayers layers;
    final private VkExtensions extensions;
    final private List<VkLayer> validationLayers = new ArrayList<>();

    private VkDebugUtilsMessengerCreateInfoEXT vkDebugMessenger = null;
    private long debugMessengerHandler = VK_NULL_HANDLE;

    private boolean vkDebugEnabled = false;

    private org.lwjgl.vulkan.VkInstance handle;

    public VkInstance( Configuration configuration ) {
        super( configuration );
        this.layers = new VkLayers( configuration );
        this.extensions = new VkExtensions( configuration );
    }

    @Override
    public void setup() throws ThemisException {
        setupInnerObjects();
        setupDebugMode();
        setupVkInstance();
        attachDebugMessengerToInstance();
        LOG.trace("Instance initialized.");
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
        destroyInstance( this.handle );
        this.handle = null;
    }

    private void vkCleanupDebugMessenger() throws ThemisException {

        if ( this.debugMessengerHandler != VK_NULL_HANDLE ) {
            destroyDebugUtilsMessengerEXT( this.handle, this.debugMessengerHandler );
            this.debugMessengerHandler = VK_NULL_HANDLE;
        }

        if ( this.vkDebugMessenger != null ) {
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
        this.vkDebugEnabled   = checkDebugMode();
        this.vkDebugMessenger = this.vkDebugEnabled ? createDebugMessenger() : null;
        LOG.tracef("[VkInstance] Debug mode enabled : %b", this.vkDebugEnabled);
    }

    private void setupVkInstance() throws ThemisException {
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            PointerBuffer extensions = selectVkExtensions( stack );
            PointerBuffer layers     = selectVkLayers( stack );
            VkInstanceCreateInfo instanceCreateInfo = createInstanceCreateInfo(stack, extensions, layers);
            this.handle = vkCreateInstance(stack, instanceCreateInfo);
        }

    }

    protected VkInstanceCreateInfo createInstanceCreateInfo( MemoryStack stack, PointerBuffer extensions, PointerBuffer layers) throws ThemisException {

        VkApplicationInfo applicationInfo = createApplicationInfo( stack );

        return VkInstanceCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pNext(this.vkDebugMessenger != null ? this.vkDebugMessenger.address() : MemoryUtil.NULL)
                .pApplicationInfo(applicationInfo)
                .ppEnabledLayerNames(layers)
                .ppEnabledExtensionNames(extensions);

    }

    private VkApplicationInfo createApplicationInfo(MemoryStack stack) throws ThemisException {
        return VkApplicationInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(stack.UTF8( getConfiguration().application().name() ))
                .applicationVersion( getConfiguration().application().version() )
                .pEngineName(stack.UTF8(getConfiguration().engine().name()))
                .engineVersion(getConfiguration().engine().version())
                .apiVersion(VK_API_VERSION_1_3);
    }

    private PointerBuffer selectVkExtensions(MemoryStack stack) throws ThemisException {

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

        if ( !getConfiguration().renderer().debug() ) {
            return false;
        }

        getValidationLayers().clear();

        getValidationLayers().addAll( this.layers.filter( VkDefaultLayers.KHRONOS_VALIDATION ) );

        if ( getValidationLayers().isEmpty() ) {
            getValidationLayers().addAll( this.layers.filter( VkDefaultLayers.LUNARG_STANDARD_VALIDATION ) );
        }

        if ( getValidationLayers().isEmpty() ) {
            getValidationLayers().addAll( this.layers.filter(
                    VkDefaultLayers.GOOGLE_THREADING,
                    VkDefaultLayers.LUNARG_PARAMETER_VALIDATION,
                    VkDefaultLayers.LUNARG_CORE_VALIDATION,
                    VkDefaultLayers.LUNARG_OBJECT_VALIDATION,
                    VkDefaultLayers.LUNARG_UNIQUE_VALIDATION ) );
        }

        return !getValidationLayers().isEmpty();

    }

    private VkDebugUtilsMessengerCreateInfoEXT createDebugMessenger() {
        return VkDebugUtilsMessengerCreateInfoEXT
                .calloc()
                .sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
                .messageSeverity(MESSAGE_SEVERITY_BITMASK)
                .messageType(MESSAGE_TYPE_BITMASK)
                .pfnUserCallback(VkInstance::debugCallback);
    }

    private void attachDebugMessengerToInstance() throws ThemisException {
        if ( this.isDebugEnabled() ) {
            try ( MemoryStack stack = MemoryStack.stackPush() ) {
                LongBuffer buffer = stack.mallocLong(1);
                createDebugUtilsMessengerEXT(this.handle, this.vkDebugMessenger, buffer);
                this.debugMessengerHandler = buffer.get(0);
            }
        }
    }

    private static int debugCallback(int messageSeverity, int messageTypes, long pCallbackData, long pUserData) {

        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

        if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT) != 0) {
            LOG.infof("[Vulkan Debug] %s", callbackData.pMessageString());
        } else if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) != 0) {
            LOG.warnf("[Vulkan Debug] %s", callbackData.pMessageString());
        } else if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0) {
            LOG.errorf("[Vulkan Debug] %s", callbackData.pMessageString());
        } else {
            LOG.debugf("[Vulkan Debug] %s", callbackData.pMessageString());
        }

        return VK_FALSE;

    }

    private org.lwjgl.vulkan.VkInstance vkCreateInstance( MemoryStack stack, VkInstanceCreateInfo instanceCreateInfo) throws ThemisException {
        PointerBuffer pInstance = stack.mallocPointer(1);
        createInstance(instanceCreateInfo, pInstance);
        return new org.lwjgl.vulkan.VkInstance(pInstance.get(0), instanceCreateInfo);
    }

}
