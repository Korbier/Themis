package org.sc.themis.renderer.base.resource.buffer;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.sc.themis.shared.utils.MemorySizeUtils;

/** VkBuffer helper */
public class VkBufferFiller {

  private VkBuffer buffer;
  private int offset;

  public static VkBufferFiller of(VkBuffer buffer) {
    VkBufferFiller filler = new VkBufferFiller();
    filler.buffer = buffer;
    return filler;
  }

  private VkBufferFiller() {}

  public VkBufferFiller reset() {
    this.offset = 0;
    return this;
  }

  public VkBufferFiller put(Vector4f value) {
    return put(value, MemorySizeUtils.VEC4F);
  }

  public VkBufferFiller put(Vector4f value, int padding) {
    this.buffer.set(this.offset, value);
    this.offset += padding;
    return this;
  }

  public VkBufferFiller put(Vector3f value) {
    return put(value, MemorySizeUtils.VEC3F);
  }

  public VkBufferFiller put(Vector3f value, int padding) {
    this.buffer.set(this.offset, value);
    this.offset += padding;
    return this;
  }

  public VkBufferFiller put(float value) {
    return put(value, MemorySizeUtils.FLOAT);
  }

  public VkBufferFiller put(float value, int padding) {
    this.buffer.set(this.offset, value);
    this.offset += padding;
    return this;
  }
}
