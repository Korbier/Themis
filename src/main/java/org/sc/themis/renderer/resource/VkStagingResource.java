package org.sc.themis.renderer.resource;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;

import java.nio.ByteBuffer;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.base.device.VkMemoryAllocator;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

public abstract sealed class VkStagingResource extends TObject
    permits VkStagingBuffer, VkStagingImage {

  private final VkDevice device;
  private final VkStagingResourceAllocator resourceAllocator;
  private final VkMemoryAllocator allocator;
  private int bufferSize;

  private VkStagingResourceStatus status = VkStagingResourceStatus.CREATED;
  private VkBuffer stagingBuffer;

  public VkStagingResource(
      Configuration configuration,
      VkStagingResourceAllocator resourceAllocator,
      VkDevice device,
      VkMemoryAllocator allocator) {
    super(configuration);
    this.resourceAllocator = resourceAllocator;
    this.device = device;
    this.allocator = allocator;
  }

  public abstract void doCommit(VkCommand command) throws ThemisException;

  @Override
  public final void setup() {}

  @Override
  public final void cleanup() throws ThemisException {
    setStatus(VkStagingResourceStatus.FREE);
    this.resourceAllocator.garbage(this);
  }

  /**
   * Called by the allocator to free this resource.
   *
   * @throws ThemisException ex
   */
  void release() throws ThemisException {
    this.cleanupStagingBuffer();
  }

  public void commit(VkCommand command) throws ThemisException {
    doCommit(command);
    setStatus(VkStagingResourceStatus.COMMITED);
  }

  public VkStagingResourceStatus getStatus() {
    return this.status;
  }

  public boolean isRenderable() {
    return getStatus() == VkStagingResourceStatus.COMMITED;
  }

  void setStatus(VkStagingResourceStatus status) {
    this.status = status;
  }

  protected void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public int getBufferSize() {
    return this.bufferSize;
  }

  public VkBuffer getStagingBuffer() {
    return this.stagingBuffer;
  }

  public void load(int buffersize, int[] data) throws ThemisException {
    load(buffersize, 0, data);
  }

  public void load(int buffersize, int offset, int[] data) throws ThemisException {
    this.recreateStagingBuffer(buffersize);
    set(offset, data);
  }

  public void load(int buffersize, float[] data) throws ThemisException {
    load(buffersize, 0, data);
  }

  public void load(int buffersize, int offset, float[] data) throws ThemisException {
    this.recreateStagingBuffer(buffersize);
    set(offset, data);
  }

  public void load(ByteBuffer bBuffer) throws ThemisException {
    this.recreateStagingBuffer(bBuffer.capacity());
    set(bBuffer);
  }

  public void set(ByteBuffer data) {
    set(() -> this.stagingBuffer.set(data));
  }

  public void set(int offset, Vector3f value) {
    set(() -> this.stagingBuffer.set(offset, value));
  }

  public void set(int offset, Vector4f value) {
    set(() -> this.stagingBuffer.set(offset, value));
  }

  public void set(int offset, Matrix4f value) {
    set(() -> this.stagingBuffer.set(offset, value));
  }

  public void set(int offset, Vector2i value) {
    set(() -> this.stagingBuffer.set(offset, value));
  }

  public void set(int offset, float value) {
    set(() -> this.stagingBuffer.set(offset, value));
  }

  public void set(int offset, int value) {
    set(() -> this.stagingBuffer.set(offset, value));
  }

  public void set(int offset, float... values) {
    set(() -> this.stagingBuffer.set(offset, values));
  }

  public void set(int offset, int... values) {
    set(() -> this.stagingBuffer.set(offset, values));
  }

  private void set(Runnable task) {
    Thread.ofVirtual()
        .start(
            () -> {
              task.run();
              setStatus(VkStagingResourceStatus.LOADED);
              this.resourceAllocator.signalResourceChanged(this);
            });
  }

  protected void setupStagingBuffer() throws ThemisException {
    VkBufferDescriptor bufferDescriptor =
        new VkBufferDescriptor(
            this.bufferSize,
            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
            VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
    this.stagingBuffer =
        new VkBuffer(getConfiguration(), this.device, this.allocator, bufferDescriptor);
    this.stagingBuffer.setup();
  }

  protected void cleanupStagingBuffer() throws ThemisException {
    this.stagingBuffer.cleanup();
    this.stagingBuffer = null;
  }

  private void recreateStagingBuffer(int buffersize) throws ThemisException {

    if (this.stagingBuffer == null || this.bufferSize != buffersize) {

      if (this.stagingBuffer != null) {
        cleanupStagingBuffer();
      }

      this.bufferSize = buffersize;
      setupStagingBuffer();

    } else {
      this.stagingBuffer.compact();
    }
  }
}
