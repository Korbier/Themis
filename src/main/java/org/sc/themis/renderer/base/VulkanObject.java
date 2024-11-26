package org.sc.themis.renderer.base;

import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.tobject.TObject;

public abstract class VulkanObject extends TObject {

    private final VulkanDebug debug = new VulkanDebug();
    private final VulkanInstance instance = new VulkanInstance();
    private final VulkanPhysicalDevice physicalDevice = new VulkanPhysicalDevice();

    public VulkanObject(Configuration configuration) {
        super( configuration );
    }

    protected VulkanDebug vkDebug() {
        return this.debug;
    }

    protected VulkanInstance vkInstance() {
        return this.instance;
    }

    protected VulkanPhysicalDevice vkPhysicalDevice() {
        return this.physicalDevice;
    }


}
