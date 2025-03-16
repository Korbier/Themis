package org.sc.themis.renderer.base.command.set;

public record VkBufferImageCopyRegion(
    long bufferOffset,
    int imageWidth, int imageHeight, int depth,
    int layerCount, int baseArrayLayer,
    int aspectMask, int mipLevel
) {
}
