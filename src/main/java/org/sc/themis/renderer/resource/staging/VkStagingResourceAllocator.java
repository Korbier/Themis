package org.sc.themis.renderer.resource.staging;

import org.jboss.logging.Logger;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.device.VkMemoryAllocator;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class VkStagingResourceAllocator extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkStagingResourceAllocator.class);

    private static final int QUEUE_SIZE = 1024;

    private final VkDevice device;
    private final VkMemoryAllocator allocator;
    private final VkCommand command;
    private final Queue<VkStagingResource> resources = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private final Queue<VkStagingResource> tracked = new ArrayBlockingQueue<>(QUEUE_SIZE);

    private VkFence commitFence;

    private AtomicBoolean garbaging = new AtomicBoolean(false);

    public VkStagingResourceAllocator(Configuration configuration, VkDevice device, VkMemoryAllocator allocator, VkCommand command) {
        super(configuration);
        this.device = device;
        this.allocator = allocator;
        this.command = command;
    }

    @Override
    public void setup() throws ThemisException {
        this.commitFence = new VkFence(getConfiguration(), this.device, false);
        this.commitFence.setup();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.commitFence.cleanup();
        for (VkStagingResource resource : this.resources) {
            resource.cleanup();
        }
        for (VkStagingResource resource : this.tracked) {
            resource.cleanup();
        }
    }

    public void commit() throws ThemisException {

        VkStagingResource resource;

        this.command.begin();

        while ((resource = this.resources.poll()) != null) {
            resource.commit(this.command);
            LOG.tracef("Staging resource commited (%d bytes)", resource.getBufferSize());
        }

        this.command.end();
        this.command.submit(this.commitFence);

        this.commitFence.waitForAndReset();

    }

    public VkStagingBuffer allocateBuffer(int bufferUsage) {
        return allocateBuffer(bufferUsage, false);
    }

    public VkStagingBuffer allocateBuffer(int bufferUsage, boolean track) {

        VkStagingBuffer buffer = new VkStagingBuffer(getConfiguration(), this, this.device, this.allocator, bufferUsage);
        buffer.setup();

        if (track) {
            this.tracked.add(buffer);
        }

        return buffer;

    }

    public VkStagingImage allocateImage(int imageFormat) {
        return this.allocateImage(imageFormat, false);
    }

    public VkStagingImage allocateImage(int imageFormat, boolean track) {

        VkStagingImage image = new VkStagingImage(getConfiguration(), this, this.device, this.allocator, imageFormat);
        image.setup();

        if (track) {
            this.tracked.add(image);
        }

        return image;

    }

    public void signalResourceChanged(VkStagingResource vkStagingResource) {
        this.resources.add(vkStagingResource);
    }

}
