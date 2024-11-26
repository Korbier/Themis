package org.sc.themis.renderer;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.renderer.device.VkInstance;
import org.sc.themis.renderer.device.VkPhysicalDevice;
import org.sc.themis.renderer.device.VkPhysicalDeviceSelectors;
import org.sc.themis.renderer.device.VkPhysicalDevices;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

public class Renderer extends TObject {

    private static final Logger LOG = Logger.getLogger(Renderer.class);

    private final RendererActivity activity;
    private final VkInstance instance;
    private VkPhysicalDevice physicalDevice;

    public Renderer(Configuration configuration, RendererActivity activity ) {
        super(configuration);
        this.activity = activity;
        this.instance = new VkInstance( configuration );
    }

    @Override
    public void setup() throws ThemisException {
        this.instance.setup();
        this.setupPhysicalDevice();
        LOG.trace( "Renderer initialized" );
    }

    private void setupPhysicalDevice() throws ThemisException {

        VkPhysicalDevices devices = new VkPhysicalDevices(getConfiguration(), this.instance);
        devices.setup();

        this.physicalDevice = devices.select( VkPhysicalDeviceSelectors.hasGraphicsQueue.and( VkPhysicalDeviceSelectors.hasKHRSwapChainExtension ) );

    }

    @Override
    public void cleanup() throws ThemisException {
        this.physicalDevice.cleanup();
        this.instance.cleanup();
    }

    public void render(Scene scene, long tpf ) {

    }

}
