package org.sc.themis.renderer.device.extension;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.IntBuffer;
import java.util.*;

public class VkExtensions extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkExtensions.class);

    final private Set<VkExtension> extensions = new HashSet<>();

    public VkExtensions(Configuration configuration) {
        super( configuration );
    }

    @Override
    public void setup() throws ThemisException {

        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.extensions.addAll(fetchExtensions(stack));

            LOG.tracef("Extensions setup. Found %d layers.", size());

            if (LOG.isTraceEnabled()) {
                showExtensions();
            }

        }
    }

    @Override
    public void cleanup() {
        this.extensions.clear();
    }

    public int size() {
        return this.extensions.size();
    }

    /**
     * Checks if the internal list of extensions contains the specified VkExtension.
     *
     * @param  ext the VkExtension to check for existence
     * @return     true if the VkExtension is found, false otherwise
     */
    public boolean contains( VkExtension ext ) {
        return this.extensions.stream().anyMatch( e -> e.getName().equals( ext.getName() ) );
    }

    /**
     * Filters the given list of VkExtensions based on the availability in the internal list of extensions.
     *
     * @param  extensions the list of VkExtensions to be filtered
     * @return             the filtered list of VkExtensions
     */
    public List<VkExtension> filter(VkExtension... extensions ) {
        return Arrays.stream(extensions).filter(this.extensions::contains).toList();
    }

    private List<VkExtension> fetchExtensions( MemoryStack stack ) throws ThemisException {

        List<VkExtension> extensions = new ArrayList<>();
        VkExtensionProperties.Buffer vkExtensions = vkFetchExtensions( stack );

        for (int i = 0; i < vkExtensions.capacity(); i++) {

            String name = vkExtensions.get(i).extensionNameString();
            VkExtension extension = VkDefaultExtensions.fromName( name );

            if ( extension == VkDefaultExtensions.UNKNOWN ) {
                extension = VkExtension.of( name );
            }

            extensions.add( extension );

        }

        return extensions;

    }

    /**
     * Fetches the Vulkan extension properties.
     *
     * @param  stack   the MemoryStack to allocate the VkExtensionProperties.Buffer
     * @return         the VkExtensionProperties.Buffer containing the fetched extension properties
     * @throws ThemisException if an error occurs during the enumeration of instance extension properties
     */
    private VkExtensionProperties.Buffer vkFetchExtensions(MemoryStack stack ) throws ThemisException {

        IntBuffer numExtArr = stack.callocInt(1);
        vkInstance().enumerateInstanceExtensionProperties( numExtArr, null );

        int numLayers = numExtArr.get(0);
        VkExtensionProperties.Buffer extensionsBuff = VkExtensionProperties.calloc(numLayers, stack);

        vkInstance().enumerateInstanceExtensionProperties( numExtArr, extensionsBuff );

        return extensionsBuff;

    }

    private void showExtensions() {
        for ( VkExtension extension : this.extensions ) {
            LOG.tracef( "Extension found : %s", extension.getName() );
        }
    }

}
