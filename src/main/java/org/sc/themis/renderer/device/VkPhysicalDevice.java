package org.sc.themis.renderer.device;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.queue.VkQueueFamily;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.lwjgl.vulkan.VK10.*;

public class VkPhysicalDevice extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkPhysicalDevice.class);

    private final org.lwjgl.vulkan.VkPhysicalDevice handle;
    private VkExtensionProperties.Buffer vkDeviceExtensions;
    private VkPhysicalDeviceMemoryProperties vkMemoryProperties;
    private VkPhysicalDeviceFeatures vkPhysicalDeviceFeatures;
    private VkPhysicalDeviceProperties vkPhysicalDeviceProperties;
    private VkQueueFamilyProperties.Buffer vkQueueFamilyProperties;

    final private List<VkQueueFamily> queueFamilies = new ArrayList<>();

    public VkPhysicalDevice(Configuration configuration, org.lwjgl.vulkan.VkPhysicalDevice handle ) {
        super(configuration);
        this.handle = handle;
    }

    @Override
    public void setup() throws ThemisException {

        try (MemoryStack stack = MemoryStack.stackPush() ) {
            this.vkDeviceExtensions = vkFetchDeviceExtensions(stack, this.handle);
            this.vkMemoryProperties = fetchMemoryProperties(this.handle);
            this.vkPhysicalDeviceFeatures = fetchPhysicalDeviceFeatures(stack, this.handle);
            this.vkPhysicalDeviceProperties = fetchPhysicalDeviceProperties(stack, this.handle);
            this.vkQueueFamilyProperties = fetchQueueFamilyProperties(stack, this.handle);
        }

        this.fetchQueueFamilies();

        LOG.tracef("  Device name : %s", this.vkPhysicalDeviceProperties.deviceNameString());
        LOG.tracef("  Extensions found : %s", this.vkDeviceExtensions.capacity());
        LOG.tracef("  Queue families found : %s", this.vkQueueFamilyProperties.capacity());

        if (LOG.isTraceEnabled()) {
            showDevicesExtensions();
            showQueueFamilyProperties();
        }

        LOG.trace("Physical device initialized");

    }

    @Override
    public void cleanup() {
        this.vkDeviceExtensions.free();
        this.vkMemoryProperties.free();
        this.vkPhysicalDeviceFeatures.free();
        this.vkPhysicalDeviceProperties.free();
        this.vkQueueFamilyProperties.free();
    }

    public org.lwjgl.vulkan.VkPhysicalDevice getHandle() {
        return this.handle;
    }

    public VkPhysicalDeviceFeatures getFeatures() {
        return this.vkPhysicalDeviceFeatures;
    }

    public VkQueueFamilyProperties.Buffer getQueueFamilyProperties() {
        return this.vkQueueFamilyProperties;
    }

    public Optional<VkQueueFamily> selectQueueFamily(Predicate<VkQueueFamily> selector ) {
        return this.queueFamilies.stream().filter( selector ).findFirst();
    }

    public VkPhysicalDeviceProperties getVkPhysicalDeviceProperties() {
        return this.vkPhysicalDeviceProperties;
    }

    public int getMaxUsableSampleCount() {

        int color = this.vkPhysicalDeviceProperties.limits().framebufferColorSampleCounts();
        int depth = this.vkPhysicalDeviceProperties.limits().framebufferDepthSampleCounts();

        int value = color & depth;

        if ( (value & VK_SAMPLE_COUNT_64_BIT) != 0 ) return VK_SAMPLE_COUNT_64_BIT;
        if ( (value & VK_SAMPLE_COUNT_32_BIT) != 0 ) return VK_SAMPLE_COUNT_32_BIT;
        if ( (value & VK_SAMPLE_COUNT_16_BIT) != 0 ) return VK_SAMPLE_COUNT_16_BIT;
        if ( (value & VK_SAMPLE_COUNT_8_BIT) != 0 )  return VK_SAMPLE_COUNT_8_BIT;
        if ( (value & VK_SAMPLE_COUNT_4_BIT) != 0 )  return VK_SAMPLE_COUNT_4_BIT;
        if ( (value & VK_SAMPLE_COUNT_2_BIT) != 0 )  return VK_SAMPLE_COUNT_2_BIT;

        return VK_SAMPLE_COUNT_1_BIT;

    }

    public boolean hasExtension( String extension ) {

        int numExtensions = vkDeviceExtensions != null ? vkDeviceExtensions.capacity() : 0;

        for (int i = 0; i < numExtensions; i++) {
            String extensionName = vkDeviceExtensions.get(i).extensionNameString();
            if ( extension.equals(extensionName) ) {
                return true;
            }
        }

        return false;

    }

    private VkExtensionProperties.Buffer vkFetchDeviceExtensions(MemoryStack stack, org.lwjgl.vulkan.VkPhysicalDevice device) throws ThemisException {

        IntBuffer intBuffer = stack.mallocInt(1);
        vkPhysicalDevice().enumerateDeviceExtensionProperties( device, intBuffer, null );

        int numProperties = intBuffer.get(0);
        VkExtensionProperties.Buffer propBuff = VkExtensionProperties.calloc(numProperties);

        vkPhysicalDevice().enumerateDeviceExtensionProperties( device, intBuffer, propBuff );

        return propBuff;

    }

    private VkPhysicalDeviceMemoryProperties fetchMemoryProperties( org.lwjgl.vulkan.VkPhysicalDevice device ) throws ThemisException {
        VkPhysicalDeviceMemoryProperties vkMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
        vkPhysicalDevice().getPhysicalDeviceMemoryProperties( device, vkMemoryProperties );
        return vkMemoryProperties;
    }

    private VkPhysicalDeviceFeatures fetchPhysicalDeviceFeatures(MemoryStack stack, org.lwjgl.vulkan.VkPhysicalDevice device) throws ThemisException  {
        VkPhysicalDeviceFeatures vkFeatures = VkPhysicalDeviceFeatures.calloc();
        vkPhysicalDevice().getPhysicalDeviceFeatures( device, vkFeatures );
        return vkFeatures;
    }

    private VkPhysicalDeviceProperties fetchPhysicalDeviceProperties(MemoryStack stack, org.lwjgl.vulkan.VkPhysicalDevice device ) throws ThemisException {
        VkPhysicalDeviceProperties vkProperties = VkPhysicalDeviceProperties.calloc();
        vkPhysicalDevice().getPhysicalDeviceProperties( device, vkProperties );
        return vkProperties;
    }

    private VkQueueFamilyProperties.Buffer fetchQueueFamilyProperties(MemoryStack stack, org.lwjgl.vulkan.VkPhysicalDevice device ) throws ThemisException {

        IntBuffer intBuffer = stack.mallocInt(1);
        vkPhysicalDevice().getPhysicalDeviceQueueFamilyProperties( device, intBuffer, null );

        int numProperties = intBuffer.get(0);
        VkQueueFamilyProperties.Buffer propBuff = VkQueueFamilyProperties.calloc(numProperties);

        vkPhysicalDevice().getPhysicalDeviceQueueFamilyProperties( device, intBuffer, propBuff );

        return propBuff;

    }

    private void fetchQueueFamilies() {

        this.queueFamilies.clear();
        int numQueueFamilies = this.vkQueueFamilyProperties.capacity();

        for (int i = 0; i < numQueueFamilies; i++) {
            VkQueueFamilyProperties familyProps = this.vkQueueFamilyProperties.get(i);
            this.queueFamilies.add( new VkQueueFamily(i, familyProps) );
        }

    }

    private void showDevicesExtensions() {
        for (int i=0; i<this.vkDeviceExtensions.capacity(); i++) {
            LOG.tracef( "Device extension found : %s ", this.vkDeviceExtensions.get(i).extensionNameString() );
        }
    }

    public int memoryTypeFromProperties( int typeBits, int reqsMask ) {

        int result = -1;

        VkMemoryType.Buffer memoryTypes = this.vkMemoryProperties.memoryTypes();

        for (int i = 0; i < VK_MAX_MEMORY_TYPES; i++) {
            if ((typeBits & 1) == 1 && (memoryTypes.get(i).propertyFlags() & reqsMask) == reqsMask) {
                result = i;
                break;
            }
            typeBits >>= 1;
        }

        if (result < 0) {
            throw new RuntimeException("Failed to find memoryType");
        }

        return result;

    }

    private void showQueueFamilyProperties() {
        for (int i=0; i<this.vkQueueFamilyProperties.capacity(); i++) {
            LOG.tracef( "Queue family found : count = %d / flag = %d ", this.vkQueueFamilyProperties.get(i).queueCount(), this.vkQueueFamilyProperties.get(i).queueFlags());
        }
    }


}
