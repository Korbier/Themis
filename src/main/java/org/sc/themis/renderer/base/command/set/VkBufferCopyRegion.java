package org.sc.themis.renderer.base.command.set;

public record VkBufferCopyRegion(long srcOffset, long dstOffset, long size) {

  public static VkBufferCopyRegion of(long srcOffset, long dstOffset, long size) {
    return new VkBufferCopyRegion(srcOffset, dstOffset, size);
  }

}
