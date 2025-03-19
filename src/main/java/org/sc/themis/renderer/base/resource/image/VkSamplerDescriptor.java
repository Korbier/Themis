package org.sc.themis.renderer.base.resource.image;

public record VkSamplerDescriptor(int filteringMode, int mipLevels, boolean anisotropyEnable, boolean compareEnable) {

  public VkSamplerDescriptor(int filteringMode, int mipLevels, boolean anisotropyEnable) {
    this( filteringMode, mipLevels, anisotropyEnable, false);
  }

  public VkSamplerDescriptor(int filteringMode, int mipLevels, boolean anisotropyEnable, boolean compareEnable) {
    this.filteringMode = filteringMode;
    this.mipLevels = mipLevels;
    this.anisotropyEnable = anisotropyEnable;
    this.compareEnable = compareEnable;
  }
}
