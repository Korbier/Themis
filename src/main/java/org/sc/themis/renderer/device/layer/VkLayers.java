package org.sc.themis.renderer.device.layer;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkLayerProperties;
import org.sc.themis.renderer.base.VkObject;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.IntBuffer;
import java.util.*;

/**
 * Represents a collection of Vulkan layers available on the system.
 * <p>
 * A Vulkan layer is a library that intercepts Vulkan function calls and
 * performs additional operations. They can be used for debugging, profiling,
 * validation, or other purposes.
 * <p>
 * This class provides a way to fetch and filter the available layers, as well
 * as to retrieve the selected validation layers.
 *
 */
public class VkLayers extends VkObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkLayers.class);

    final private Set<VkLayer> layers = new HashSet<>();

    public VkLayers(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void setup() throws ThemisException {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            this.layers.addAll(fetchLayers(stack));

            LOG.debugf("Layers setup. Found %d layers.", size());

            if (LOG.isTraceEnabled()) {
                showLayers();
            }

        }
    }

    @Override
    public void cleanup() {
        this.layers.clear();
    }

    public int size() {
        return this.layers.size();
    }

    /**
     * Checks if the internal list of layers contains the specified VkLayer.
     *
     * @param  layer the VkLayer to check for existence
     * @return       true if the VkLayer is found, false otherwise
     */
    public boolean contains( VkLayer layer ) {
        return this.layers.stream().anyMatch( l -> l.getName().equals( layer.getName() ) );
    }

    /**
     * Filters the given list of VkLayers based on the availability in the internal list of layers.
     *
     * @param  requestedLayers the list of VkLayers to be filtered
     * @return                 the filtered list of VkLayers
     */
    public List<VkLayer> filter(VkLayer... requestedLayers ) {
        return Arrays.stream(requestedLayers).filter(this.layers::contains).toList();
    }

    /**
     * Fetches the list of Vulkan layers available in the system.
     *
     * @param  stack   the MemoryStack to allocate the VkLayerProperties.Buffer
     * @return         the list of VkLayer objects representing the available layers
     * @throws ThemisException if an error occurs during the enumeration of instance layer properties
     */
    private List<VkLayer> fetchLayers(MemoryStack stack ) throws ThemisException {

        List<VkLayer> layers = new ArrayList<>();
        VkLayerProperties.Buffer vkLayers = vkFetchLayers( stack );

        for (int i = 0; i < vkLayers.capacity(); i++) {

            String layername = vkLayers.get(i).layerNameString();
            VkLayer layer = VkDefaultLayers.fromName( layername );

            if ( layer == VkDefaultLayers.UNKNOWN ) {
                layer = VkLayer.of( layername );
            }

            layers.add( layer );

        }

        return layers;

    }

    /**
     * Fetches the Vulkan layer properties.
     *
     * @param  stack   the MemoryStack to allocate the VkLayerProperties.Buffer
     * @return         the VkLayerProperties.Buffer containing the fetched layer properties
     * @throws ThemisException if an error occurs during the enumeration of instance layer properties
     */
    private VkLayerProperties.Buffer vkFetchLayers(MemoryStack stack ) throws ThemisException {

        IntBuffer numLayersArr = stack.callocInt(1);
        enumerateInstanceLayerProperties( numLayersArr, null );

        int numLayers = numLayersArr.get(0);
        VkLayerProperties.Buffer layersBuff = VkLayerProperties.calloc(numLayers, stack);

        enumerateInstanceLayerProperties( numLayersArr, layersBuff );

        return layersBuff;

    }

    private void showLayers() {
        for ( VkLayer layer : layers ) {
            LOG.tracef( "Layer found : %s", layer.getName() );
        }
    }
}
