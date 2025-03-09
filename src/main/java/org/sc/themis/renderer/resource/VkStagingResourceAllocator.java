package org.sc.themis.renderer.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import org.jboss.logging.Logger;
import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.base.device.VkMemoryAllocator;
import org.sc.themis.renderer.base.sync.VkFence;
import org.sc.themis.renderer.lang.VulkanObject;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public class VkStagingResourceAllocator extends VulkanObject {

  private static final org.jboss.logging.Logger LOG =
      Logger.getLogger(VkStagingResourceAllocator.class);

  private static final int STAGING_SIZE = 1024;
  private static final int ALIVED_SIZE = 1024;
  private static final int GARBAGE_SIZE = 1024;

  private final VkDevice device;
  private final VkMemoryAllocator allocator;
  private final Queue<VkStagingResource> staging = new ArrayBlockingQueue<>(STAGING_SIZE);
  private final List<VkStagingResource> staged =
      Collections.synchronizedList(new ArrayList<>(ALIVED_SIZE));
  private final Queue<VkStagingResource> garbage = new ArrayBlockingQueue<>(GARBAGE_SIZE);

  private VkFence commitFence;

  public VkStagingResourceAllocator(
      Configuration configuration, VkDevice device, VkMemoryAllocator allocator) {
    super(configuration);
    this.device = device;
    this.allocator = allocator;
  }

  @Override
  public void setup() throws ThemisException {

    this.commitFence = new VkFence(getConfiguration(), this.device, false);
    this.commitFence.setup();
  }

  @Override
  public void cleanup() throws ThemisException {
    this.commitFence.cleanup();
    garbageAndReleaseAll();
  }

  public void submit(VkCommand command) throws ThemisException {

    VkStagingResource resource;
    command.begin();

    while ((resource = this.staging.poll()) != null) {
      resource.commit(command);
      this.staged.add(resource);
      LOG.tracef("Staging resource commited (%d bytes)", resource.getBufferSize());
    }

    command.end();
    command.submit(this.commitFence);

    this.commitFence.waitForAndReset();

    releaseAllInThread();
  }

  public VkStagingBuffer allocateBuffer(int bufferUsage) {
    VkStagingBuffer buffer =
        new VkStagingBuffer(getConfiguration(), this, this.device, this.allocator, bufferUsage);
    buffer.setup();
    return buffer;
  }

  public VkStagingImage allocateImage(int imageFormat) {
    VkStagingImage image =
        new VkStagingImage(getConfiguration(), this, this.device, this.allocator, imageFormat);
    image.setup();
    return image;
  }

  void signalResourceChanged(VkStagingResource vkStagingResource) {
    this.staging.add(vkStagingResource);
  }

  synchronized void garbage(VkStagingResource vkStagingResource) {
    this.staged.remove(vkStagingResource);
    this.garbage.add(vkStagingResource);
  }

  private void releaseAll() throws ThemisException {
    LOG.tracef("Releasing garbaged resources");
    VkStagingResource resource;
    while ((resource = this.garbage.poll()) != null) {
      resource.release();
      LOG.tracef("Garbaging resource (%d bytes)", resource.getBufferSize());
    }
  }

  private void releaseAllInThread() {
    if (!this.garbage.isEmpty()) {
      Thread.ofVirtual()
          .start(
              () -> {
                try {
                  releaseAll();
                } catch (ThemisException e) {
                  throw new RuntimeException(e);
                }
              });
    }
  }

  private void garbageAndReleaseAll() throws ThemisException {
    LOG.tracef("Garbaging all resources");
    new ArrayList<>(this.staged).forEach(this::garbage);
    releaseAll();
  }
}
