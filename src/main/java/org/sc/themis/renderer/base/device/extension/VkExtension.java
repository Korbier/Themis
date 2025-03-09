package org.sc.themis.renderer.base.device.extension;

public interface VkExtension {

  static VkExtension of(String name) {
    return () -> name;
  }

  String getName();
}
