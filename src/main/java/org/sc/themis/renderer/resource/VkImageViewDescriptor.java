package org.sc.themis.renderer.resource;

public record VkImageViewDescriptor(
    int aspectMask,
    int baseArrayLayer,
    int format,
    int layerCount,
    int mipLevels,
    int viewType
) {
}
