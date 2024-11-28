package org.sc.themis.renderer.resource.image;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;

public record VkImageDescriptor(
    int format, //a VkFormat describing the format and type of the texel blocks that will be contained in the image.
    int mipLevel, //the number of levels of detail available for minified sampling of the image.
    int width,  //a VkExtent3D (width component) describing the number of data elements in each dimension of the base level.
    int height, //a VkExtent3D (height component)describing the number of data elements in each dimension of the base level.
    int samples, //a VkSampleCountFlagBits value specifying the number of samples per texel.
    int arrayLayers, //the number of layers in the image.
    int usage, //a bitmask of VkImageUsageFlagBits describing the intended bufferUsage of the image.
    int memoryTypeIndex, //an index identifying a memory type from the memoryTypes array of the VkPhysicalDeviceMemoryProperties structure.
    int tiling
) {

    public VkImageDescriptor(int format, int mipLevel, int width, int height, int samples, int arrayLayers, int usage, int memoryTypeIndex, int tiling) {
        this.format = format;
        this.mipLevel = mipLevel;
        this.width = width;
        this.height = height;
        this.samples = samples;
        this.arrayLayers = arrayLayers;
        this.usage = usage;
        this.memoryTypeIndex = memoryTypeIndex;
        this.tiling = tiling;
    }

    public VkImageDescriptor(int format, int mipLevel, int width, int height, int samples, int arrayLayers, int usage, int memoryTypeIndex) {
        this(format, mipLevel, width, height, samples, arrayLayers, usage, memoryTypeIndex, VK_IMAGE_TILING_OPTIMAL);
    }

}
