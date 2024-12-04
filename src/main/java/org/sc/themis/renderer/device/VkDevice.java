package org.sc.themis.renderer.device;

import org.jboss.logging.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.exception.NoQueueFamilyFoundException;
import org.sc.themis.renderer.presentation.VkSurface;
import org.sc.themis.renderer.queue.VkQueue;
import org.sc.themis.renderer.queue.VkQueueFamily;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.BitwiseState;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.lwjgl.vulkan.VK10.*;

public class VkDevice extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkDevice.class);

    final public static int FEATURE_SAMPLER_ANISOTROPY = 0b0000_0000_0000_0000_0000_0000_0000_0001;
    final public static int FEATURE_GEOMETRY_SHADER    = 0b0000_0000_0000_0000_0000_0000_0000_0010;

    final private VkPhysicalDevice physicalDevice;
    private org.lwjgl.vulkan.VkDevice handle;

    private final BitwiseState features = new BitwiseState();

    public VkDevice( Configuration configuration, VkPhysicalDevice physicalDevice) {
        super(configuration);
        this.physicalDevice = physicalDevice;
    }

    public org.lwjgl.vulkan.VkDevice getHandle() {
        return this.handle;
    }

    public VkPhysicalDevice getPhysicalDevice() {
        return this.physicalDevice;
    }

    @Override
    public void setup() throws ThemisException {
        setupVkDevice();
        LOG.trace("Device initialized");

    }

    @Override
    public void cleanup() throws ThemisException {
        cleanupVkDevice();
    }

    public void waitIdle() throws ThemisException {
        vkDevice().deviceWaitIdle( this.getHandle() );
    }

    public VkQueue selectQueue( int queueIndex, Predicate<VkQueueFamily> selector ) throws ThemisException {

        int queueFamilyIndex = selectQueueFamily( selector );
        org.lwjgl.vulkan.VkQueue vkQueue = vkFetchQueue( queueIndex, queueFamilyIndex);

        VkQueue queue = new VkQueue( getConfiguration(), vkQueue, queueFamilyIndex );
        queue.setup();

        return queue;

    }

    public VkQueue selectPresentQueue( int queueIndex, VkSurface surface ) throws ThemisException {

        Predicate<VkQueueFamily> selector = (queueFamily) -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer intBuff = stack.mallocInt(1);
                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR( this.physicalDevice.getHandle(), queueIndex, surface.getHandle(), intBuff);
                return intBuff.get(0) == VK_TRUE;
            }
        };

        return selectQueue( queueIndex, selector );

    }

    public boolean isFeatureEnabled( int feature ) {
        return this.features.isset( feature );
    }

    private void setupVkDevice() throws ThemisException {
        try ( MemoryStack stack = MemoryStack.stackPush() ){
            PointerBuffer requiredExtensions = selectVkExtensions(stack);
            VkPhysicalDeviceFeatures requiredFeatures = selectVkFeatures(stack);
            VkDeviceQueueCreateInfo.Buffer queueCreateInfo = createQueueCreateInfo(stack);
            VkDeviceCreateInfo deviceCreateInfo = createDeviceCreateInfo(stack, requiredExtensions, requiredFeatures, queueCreateInfo);
            this.handle = this.vkCreateDevice(stack, deviceCreateInfo);
        }
    }

    private void cleanupVkDevice() throws ThemisException {
        vkDevice().destroyDevice( this.getHandle() );
    }

    private PointerBuffer selectVkExtensions(MemoryStack stack) {

        List<String> extensions = new ArrayList<>();
        extensions.add( KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME );

        PointerBuffer required = stack.mallocPointer( extensions.size() );
        for ( String extension : extensions ) {
            required.put(stack.ASCII(extension));
        }

        required.flip();

        return required;

    }

    private VkPhysicalDeviceFeatures selectVkFeatures( MemoryStack stack ) {

        VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc(stack);

        if ( getConfiguration().renderer().feature().samplerAnisotropy() && this.physicalDevice.getFeatures().samplerAnisotropy() ) {
            LOG.info( "Sampler Anisotropy feature enabled");
            this.features.set( FEATURE_SAMPLER_ANISOTROPY );
            features.samplerAnisotropy( true );
        }

        if ( getConfiguration().renderer().feature().geometryShader() && this.physicalDevice.getFeatures().geometryShader() ) {
            LOG.info( "Geometry Shader feature enabled");
            this.features.set( FEATURE_GEOMETRY_SHADER );
            features.geometryShader( true );
        }

        return features;

    }

    private VkDeviceQueueCreateInfo.Buffer createQueueCreateInfo(MemoryStack stack) {

        VkQueueFamilyProperties.Buffer queueFamilyProperties = this.physicalDevice.getQueueFamilyProperties();
        int                            numQueuesFamilies = queueFamilyProperties.capacity();
        VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(numQueuesFamilies, stack);

        for (int i = 0; i < numQueuesFamilies; i++) {
            FloatBuffer priorities = stack.callocFloat( queueFamilyProperties.get(i).queueCount() );
            queueCreateInfos.get(i)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    .queueFamilyIndex(i)
                    .pQueuePriorities(priorities)
            ;
        }

        return queueCreateInfos;

    }

    private VkDeviceCreateInfo createDeviceCreateInfo(MemoryStack stack, PointerBuffer requiredExtensions, VkPhysicalDeviceFeatures requiredFeatures, VkDeviceQueueCreateInfo.Buffer queueCreateInfo) {
        return VkDeviceCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .ppEnabledExtensionNames(requiredExtensions)
                .pEnabledFeatures(requiredFeatures)
                .pQueueCreateInfos(queueCreateInfo);
    }

    private org.lwjgl.vulkan.VkDevice vkCreateDevice(MemoryStack stack, VkDeviceCreateInfo deviceCreateInfo) throws ThemisException {
        PointerBuffer pp = stack.mallocPointer(1);
        vkDevice().createDevice( this.physicalDevice.getHandle(), deviceCreateInfo, pp );
        return new org.lwjgl.vulkan.VkDevice(pp.get(0), this.physicalDevice.getHandle(), deviceCreateInfo);
    }

    private int selectQueueFamily( Predicate<VkQueueFamily> selector ) throws NoQueueFamilyFoundException {
        return getPhysicalDevice()
                .selectQueueFamily( selector )
                .orElseThrow(NoQueueFamilyFoundException::new)
                .handle();
    }

    private org.lwjgl.vulkan.VkQueue vkFetchQueue( int queueIndex, int queueFamilyIndex ) throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            PointerBuffer pQueue = stack.mallocPointer(1);
            vkDevice().getDeviceQueue( getHandle(), queueFamilyIndex, queueIndex, pQueue);
            return new org.lwjgl.vulkan.VkQueue(pQueue.get(0), getHandle());
        }
    }


}
